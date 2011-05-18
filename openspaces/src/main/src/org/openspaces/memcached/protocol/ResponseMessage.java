package org.openspaces.memcached.protocol;

import org.openspaces.memcached.LocalCacheElement;
import org.openspaces.memcached.SpaceCache;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Represents the response to a command.
 */
public final class ResponseMessage implements Serializable {

    private static final long serialVersionUID = -363616355081114688L;

    public ResponseMessage(CommandMessage cmd) {
        this.cmd = cmd;
    }

    public CommandMessage cmd;
    public LocalCacheElement[] elements;
    public SpaceCache.StoreResponse response;
    public Map<String, Set<String>> stats;
    public String version;
    public SpaceCache.DeleteResponse deleteResponse;
    public Integer incrDecrResponse;
    public boolean flushSuccess;

    public ResponseMessage withElements(LocalCacheElement[] elements) {
        this.elements = elements;
        return this;
    }

    public ResponseMessage withResponse(SpaceCache.StoreResponse response) {
        this.response = response;
        return this;
    }

    public ResponseMessage withDeleteResponse(SpaceCache.DeleteResponse deleteResponse) {
        this.deleteResponse = deleteResponse;
        return this;
    }

    public ResponseMessage withIncrDecrResponse(Integer incrDecrResp) {
        this.incrDecrResponse = incrDecrResp;

        return this;
    }

    public ResponseMessage withStatResponse(Map<String, Set<String>> stats) {
        this.stats = stats;

        return this;
    }

    public ResponseMessage withFlushResponse(boolean success) {
        this.flushSuccess = success;

        return this;
    }
}