package com.juaracoding.DBLaundry.repo;

import com.juaracoding.DBLaundry.model.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MenuRepo extends JpaRepository<Menu,Long> {

    Page<Menu> findByIsDelete(Pageable page , byte byteIsDelete);
    Page<Menu> findByIsDeleteAndNamaMenuContainsIgnoreCase(Pageable page , byte byteIsDelete, String values);
    Page<Menu> findByIsDeleteAndPathMenuContainsIgnoreCase(Pageable page , byte byteIsDelete, String values);
    Page<Menu> findByIsDeleteAndEndPointContainsIgnoreCase(Pageable page , byte byteIsDelete, String values);
    Page<Menu> findByIsDeleteAndIdMenuContainsIgnoreCase(Pageable page , byte byteIsDelete, Long values);

}