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

package org.openspaces.archive;

import org.openspaces.archive.ArchiveOperationHandler;

/**
 * Exposes an {@link ArchiveOperationHandler} instance
 *
 * @author Itai Frenkel
 * @since 9.1.1
 */
public interface ArchiveOperationHandlerProvider {

    /**
     * @return an instance of {@link ArchiveOperationHandler}
     */
    ArchiveOperationHandler getArchiveOperationHandler();
}
