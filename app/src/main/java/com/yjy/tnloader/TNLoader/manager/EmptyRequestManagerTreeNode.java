package com.yjy.tnloader.TNLoader.manager;

import com.yjy.tnloader.TNLoader.RequestManager;

import java.util.Collections;
import java.util.Set;

/**
 * Created by software1 on 2018/1/30.
 */

final class EmptyRequestManagerTreeNode implements RequestManagerTreeNode {
    @Override
    public Set<RequestManager> getDescendants() {
        return Collections.emptySet();
    }
}
