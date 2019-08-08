package com.richard.weger.wqc.helper;

import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.stereotype.Component;

import com.richard.weger.wqc.domain.DomainEntity;

@Component
public class AbstractHandler<T extends DomainEntity> extends AbstractRepositoryEventListener<T> {

}
