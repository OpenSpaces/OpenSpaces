package org.openspaces.memcached.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.openspaces.memcached.SpaceCache;
import org.openspaces.memcached.protocol.binary.MemcachedBinaryCommandDecoder;
import org.openspaces.memcached.protocol.binary.MemcachedBinaryResponseEncoder;
import org.openspaces.memcached.protocol.text.MemcachedCommandDecoder;
import org.openspaces.memcached.protocol.text.MemcachedFrameDecoder;
import org.openspaces.memcached.protocol.text.MemcachedResponseEncoder;

/**
 * @author kimchy (shay.banon)
 */
public class UnifiedProtocolDecoder extends FrameDecoder {

    private final SpaceCache cache;

    private final DefaultChannelGroup channelGroup;
    public final String version;

    public final int idle_limit;
    public final boolean verbose;

    public UnifiedProtocolDecoder(SpaceCache cache, DefaultChannelGroup channelGroup, String version, int idle_limit, boolean verbose) {
        this.cache = cache;
        this.channelGroup = channelGroup;
        this.version = version;
        this.idle_limit = idle_limit;
        this.verbose = verbose;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        if (buffer.readableBytes() < 1) {
            return null;
        }

        int magic = buffer.getUnsignedByte(buffer.readerIndex());
        if (magic == 0x80) {
            // binary protocol
            ChannelPipeline p = ctx.getPipeline();
            p.addLast("decoder", new MemcachedBinaryCommandDecoder());
            p.addLast("handler", new MemcachedCommandHandler(cache, version, verbose, idle_limit, channelGroup));
            p.addLast("encoder", new MemcachedBinaryResponseEncoder());
            p.remove(this);
        } else {
            SessionStatus status = new SessionStatus().ready();
            ChannelPipeline p = ctx.getPipeline();
            p.addLast("frame", new MemcachedFrameDecoder(status, 32768 * 1024));
            p.addLast("decoder", new MemcachedCommandDecoder(status));
            p.addLast("handler", new MemcachedCommandHandler(cache, version, verbose, idle_limit, channelGroup));
            p.addLast("encoder", new MemcachedResponseEncoder());
            p.remove(this);
        }
        // Forward the current read buffer as is to the new handlers.
        return buffer.readBytes(buffer.readableBytes());
    }
}
