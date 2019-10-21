/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.soul.plugins.http;

import com.netflix.hystrix.HystrixCommand;
import org.dromara.plugins.api.AbstractSoulPlugin;
import org.dromara.plugins.api.SoulPluginChain;
import org.dromara.soul.cache.api.data.SelectorData;
import org.dromara.soul.cache.api.service.CacheService;
import org.dromara.plugins.api.dto.SoulRequest;
import org.dromara.plugins.api.dto.SoulResponse;
import org.dromara.soul.common.enums.PluginEnum;
import org.dromara.soul.common.enums.PluginTypeEnum;
import org.dromara.soul.common.extension.ExtensionLoader;
import org.dromara.soul.plugins.http.hystrix.HttpHystrix;
import org.dromara.soul.common.utils.GsonUtils;
import org.dromara.soul.plugins.http.balance.LoadBalance;
import org.dromara.soul.plugins.http.hystrix.HttpCommand;
import org.dromara.soul.plugins.http.hystrix.HystrixBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * The type Http plugin.
 *
 * @author xiaoyu(Myth)
 */
public class HttpPlugin extends AbstractSoulPlugin {

    public HttpPlugin(CacheService cacheService) {
        super(cacheService);
    }

    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.FUNCTION;
    }

    @Override
    public int getOrder() {
        return PluginEnum.HTTP.getOrder();
    }

    @Override
    public String named() {
        return PluginEnum.HTTP.getName();
    }

    @Override
    public Boolean skip(SoulRequest soulRequest) {
        return false;
    }

    @Override
    protected SoulResponse doExecute(SoulRequest soulRequest, SelectorData selectorData, SoulPluginChain chain) {

        String handle = selectorData.getHandle();

        final HttpHystrix httpHystrix = GsonUtils.getInstance().fromJson(handle, HttpHystrix.class);

        LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getJoin(httpHystrix.getLoadBalance());

        loadBalance.select(null, "");

        HystrixCommand.Setter setter = HystrixBuilder.build(httpHystrix);

        HttpCommand command = new HttpCommand(setter);
        Future<Object> future = command.queue();

        try {
            Object result = future.get();

            //结果处理
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return chain.execute(soulRequest);
    }
}
