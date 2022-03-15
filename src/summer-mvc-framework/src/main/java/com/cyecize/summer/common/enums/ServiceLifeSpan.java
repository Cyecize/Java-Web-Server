package com.cyecize.summer.common.enums;

import com.cyecize.ioc.enums.ScopeType;

public enum ServiceLifeSpan {
    SINGLETON(ScopeType.SINGLETON), SESSION(ScopeType.PROXY), REQUEST(ScopeType.PROXY), PROTOTYPE(ScopeType.PROTOTYPE);

    private final ScopeType scopeType;

    ServiceLifeSpan(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public ScopeType getScopeType() {
        return this.scopeType;
    }
}
