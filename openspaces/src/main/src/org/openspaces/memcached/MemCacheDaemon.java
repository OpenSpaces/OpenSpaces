package org.openspaces.memcached;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.openspaces.core.GigaSpace;
import org.openspaces.memcached.protocol.binary.MemcachedBinaryPipelineFactory;
import org.openspaces.memcached.protocol.text.MemcachedPipelineFactory;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.pu.service.ServiceMonitorsProvider;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author kimchy (shay.banon)
 */
public class MemCacheDaemon implements InitializingBean, DisposableBean, BeanNameAware, ServiceDetailsProvider, ServiceMonitorsProvider {

    protected final Log logger = LogFactory.getLog(getClass());

    public final static String memcachedVersion = "0.9";

    private GigaSpace space;

    private String beanName = "memcached";

    private boolean binary;

    private String host;

    private int port;

    private int portRetries = 20;

    private int frameSize = 32768 * 1024;
    private int idleTime;

    private int boundedPort;
    private ServerSocketChannelFactory channelFactory;
    private DefaultChannelGroup allChannels;
    private SpaceCache cache;

    public void setSpace(GigaSpace space) {
        this.space = space;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPortRetries(int portRetries) {
        this.portRetries = portRetries;
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void afterPropertiesSet() throws Exception {
        cache = new SpaceCache(space);
        channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

        allChannels = new DefaultChannelGroup("memcachedChannelGroup");

        ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);

        ChannelPipelineFactory pipelineFactory;
        if (binary)
            pipelineFactory = createMemcachedBinaryPipelineFactory(cache, memcachedVersion, false, idleTime, allChannels);
        else
            pipelineFactory = createMemcachedPipelineFactory(cache, memcachedVersion, false, idleTime, frameSize, allChannels);

        bootstrap.setPipelineFactory(pipelineFactory);
        bootstrap.setOption("sendBufferSize", 65536);
        bootstrap.setOption("receiveBufferSize", 65536);

        InetAddress address = null;
        if (host != null) {
            address = InetAddress.getByName(host);
        }

        Exception lastException = null;
        boolean success = false;
        int i;
        for (i = 0; i < portRetries; i++) {
            try {
                Channel serverChannel = bootstrap.bind(new InetSocketAddress(address, port + i));
                allChannels.add(serverChannel);
                success = true;
                break;
            } catch (Exception e) {
                lastException = e;
            }
        }
        if (!success) {
            throw lastException;
        }
        boundedPort = port + i;
        logger.info("memcached started on port [" + boundedPort + "]");
    }

    protected ChannelPipelineFactory createMemcachedBinaryPipelineFactory(
            Cache cache, String memcachedVersion, boolean verbose, int idleTime, DefaultChannelGroup allChannels) {
        return new MemcachedBinaryPipelineFactory(cache, memcachedVersion, verbose, idleTime, allChannels);
    }

    protected ChannelPipelineFactory createMemcachedPipelineFactory(
            Cache cache, String memcachedVersion, boolean verbose, int idleTime, int receiveBufferSize, DefaultChannelGroup allChannels) {
        return new MemcachedPipelineFactory(cache, memcachedVersion, verbose, idleTime, receiveBufferSize, allChannels);
    }

    public void destroy() throws Exception {
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();
        if (!future.isCompleteSuccess()) {
            throw new RuntimeException("failure to complete closing all network channels");
        }
        try {
            cache.close();
        } catch (IOException e) {
            throw new RuntimeException("exception while closing storage", e);
        }
        channelFactory.releaseExternalResources();
        logger.info("memcached destroyed");
    }

    public ServiceDetails[] getServicesDetails() {
        return new ServiceDetails[] {new MemcachedServiceDetails(beanName, space.getName(), boundedPort)};
    }

    public ServiceMonitors[] getServicesMonitors() {
        return new ServiceMonitors[] {new MemcachedServiceMonitors(beanName, cache.getGetCmds(), cache.getSetCmds(), cache.getGetHits(), cache.getGetMisses())};
    }
}
