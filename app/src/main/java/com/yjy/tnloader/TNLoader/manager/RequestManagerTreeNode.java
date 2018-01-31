package com.yjy.tnloader.TNLoader.manager;

import com.yjy.tnloader.TNLoader.RequestManager;

import java.util.Set;

/**
 * Created by software1 on 2018/1/30.
 */

public interface RequestManagerTreeNode {
    /**
     * Returns all descendant {@link RequestManager}s relative to the context of the current {@link RequestManager}.
     */
    Set<RequestManager> getDescendants();
}
