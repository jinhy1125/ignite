/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.visor.cache.index;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.ignite.IgniteException;
import org.apache.ignite.internal.cache.query.index.Index;
import org.apache.ignite.internal.cache.query.index.sorted.inline.InlineIndexImpl;
import org.apache.ignite.internal.management.cache.CacheIndexesListCommandArg;
import org.apache.ignite.internal.processors.cache.GridCacheContext;
import org.apache.ignite.internal.processors.task.GridInternal;
import org.apache.ignite.internal.visor.VisorJob;
import org.apache.ignite.internal.visor.VisorOneNodeTask;
import org.jetbrains.annotations.Nullable;

/**
 * Task that collects indexes information.
 */
@GridInternal
public class IndexListTask extends VisorOneNodeTask<CacheIndexesListCommandArg, Set<IndexListInfoContainer>> {
    /** */
    private static final long serialVersionUID = 0L;

    /** Empty group name. */
    public static final String EMPTY_GROUP_NAME = "no_group";

    /** {@inheritDoc} */
    @Override protected IndexListJob job(CacheIndexesListCommandArg arg) {
        return new IndexListJob(arg, debug);
    }

    /** */
    private static class IndexListJob extends VisorJob<CacheIndexesListCommandArg, Set<IndexListInfoContainer>> {
        /** */
        private static final long serialVersionUID = 0L;

        /**
         * Create job with specified argument.
         *
         * @param arg Job argument.
         * @param debug Flag indicating whether debug information should be printed into node log.
         */
        protected IndexListJob(@Nullable CacheIndexesListCommandArg arg, boolean debug) {
            super(arg, debug);
        }

        /** {@inheritDoc} */
        @Override protected Set<IndexListInfoContainer> run(@Nullable CacheIndexesListCommandArg arg) throws IgniteException {
            if (arg == null)
                throw new IgniteException("CacheIndexesListCommandArg is null");

            Pattern indexesPtrn = getPattern(arg.indexName());
            Pattern groupsPtrn = getPattern(arg.groupName());
            Pattern cachesPtrn = getPattern(arg.cacheName());

            Set<IndexListInfoContainer> idxInfos = new HashSet<>();

            for (GridCacheContext<?, ?> ctx : ignite.context().cache().context().cacheContexts()) {
                final String cacheName = ctx.name();

                final String grpName = ctx.config().getGroupName();
                final String grpNameToValidate = grpName == null ? EMPTY_GROUP_NAME : grpName;

                if (!isNameValid(groupsPtrn, grpNameToValidate))
                    continue;

                if (!isNameValid(cachesPtrn, cacheName))
                    continue;

                Collection<Index> idxs = ignite.context().indexProcessor().indexes(cacheName);

                for (Index idx : idxs) {
                    if (!isNameValid(indexesPtrn, idx.name()))
                        continue;

                    InlineIndexImpl idx0 = idx.unwrap(InlineIndexImpl.class);

                    if (idx0 != null)
                        idxInfos.add(constructContainer(ctx, idx0));
                }
            }

            return idxInfos;
        }

        /** */
        @Nullable private Pattern getPattern(String regex) {
            return regex == null ? null : Pattern.compile(regex.toLowerCase());
        }

        /** */
        private static IndexListInfoContainer constructContainer(GridCacheContext<?, ?> ctx, InlineIndexImpl idx) {
            return new IndexListInfoContainer(
                ctx,
                idx.indexDefinition().idxName().idxName(),
                idx.indexDefinition().indexKeyDefinitions().keySet(),
                idx.indexDefinition().idxName().tableName()
            );
        }

        /** */
        private static boolean isNameValid(Pattern pattern, String name) {
            if (pattern == null)
                return true;

            return pattern.matcher(name.toLowerCase()).find();
        }
    }
}
