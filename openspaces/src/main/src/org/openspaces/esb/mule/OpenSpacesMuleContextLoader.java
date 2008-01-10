/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.esb.mule;

import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.umo.UMOManagementContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * <code> OpenSpacesMuleContextLoader</code> used for loading Mule configuration that refrenced from PU configuration
 * file.
 *
 * <p>It sets the PU appliction context as the parent of Mule appliction context, giving it the ability to access beans
 * that declerd in the PU appliction context.
 *
 * @author yitzhaki
 */
public class OpenSpacesMuleContextLoader implements ApplicationContextAware, InitializingBean, DisposableBean, ApplicationListener {

    private String location;

    private ApplicationContext applicationContext;

    private UMOManagementContext umoManagementContext;

    private volatile boolean contextCreated = false;

    public OpenSpacesMuleContextLoader() {
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    public void afterPropertiesSet() throws Exception {
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            if (!contextCreated) {
                contextCreated = true;
                SpringXmlConfigurationBuilder muleXmlConfigurationBuilder = new SpringXmlConfigurationBuilder(this.applicationContext);
                try {
                    umoManagementContext = muleXmlConfigurationBuilder.configure(location);
                    umoManagementContext.start();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to start mule [" + location + "]", e);
                }
            }
        }
    }

    public void destroy() throws Exception {
        if (umoManagementContext != null) {
            umoManagementContext.dispose();
        }
    }
}
