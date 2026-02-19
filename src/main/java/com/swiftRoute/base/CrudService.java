package com.swiftRoute.base;

import java.util.List;

public interface CrudService <T,ID>{
//    T add(Object... args);
    T add(T entity);

    T update(T entity);

    void deleteById(ID id);

    T getById(ID id);

    List<T> getAll();
}
