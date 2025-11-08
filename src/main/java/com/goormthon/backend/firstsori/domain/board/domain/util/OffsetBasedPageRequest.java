package com.goormthon.backend.firstsori.domain.board.domain.util;

import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

/**
 * offset과 limit을 직접 지정할 수 있는 커스텀 Pageable 구현체
 * Spring Data JPA가 실제로 사용하는 메서드: getOffset(), getPageSize(), getSort()
 */
@Getter
public class OffsetBasedPageRequest implements Pageable, Serializable {

    private final int offset;
    private final int limit;
    private final Sort sort;

    public OffsetBasedPageRequest(int offset, int limit, Sort sort) {
        this.offset = offset;
        this.limit = limit;
        this.sort = sort != null ? sort : Sort.unsorted();
    }

    // Spring Data JPA가 실제로 사용하는 메서드들
    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    // Pageable 인터페이스 필수 메서드 (실제로는 거의 사용 안 됨)
    @Override
    public int getPageNumber() {
        return (int) (getOffset() / getPageSize());
    }

    @Override
    public Pageable next() {
        return new OffsetBasedPageRequest((int) (getOffset() + getPageSize()), getPageSize(), getSort());
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? new OffsetBasedPageRequest(Math.max(0, (int) (getOffset() - getPageSize())), getPageSize(), getSort()) : this;
    }

    @Override
    public Pageable first() {
        return new OffsetBasedPageRequest(0, getPageSize(), getSort());
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetBasedPageRequest(pageNumber * getPageSize(), getPageSize(), getSort());
    }

    @Override
    public boolean hasPrevious() {
        return getOffset() > 0;
    }
}

