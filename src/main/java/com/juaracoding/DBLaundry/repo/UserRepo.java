package com.juaracoding.DBLaundry.repo;

import com.juaracoding.DBLaundry.model.Userz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserRepo extends JpaRepository<Userz,Long> {

    public List<Userz> findByEmail(String value);
    public List<Userz> findByEmailOrNoHPOrUsername(String emails, String noHP, String userName);

    Page<Userz> findByIsDeleteAndNoHPContainsIgnoreCase(Pageable pageable, byte b, String paramValue);

    Page<Userz> findByIsDeleteAndUsernameContainsIgnoreCase(Pageable pageable, byte b, String paramValue);

    Page<Userz> findByIsDeleteAndEmailContainsIgnoreCase(Pageable pageable, byte b, String paramValue);

    Page<Userz> findByIsDeleteAndNamaLengkapContainsIgnoreCase(Pageable pageable, byte b, String paramValue);

    Page<Userz> findByIsDeleteAndIdUser(Pageable pageable, byte b, long parseLong);

    Page<Userz> findByIsDelete(Pageable pageable, byte b);
    List<Userz> findByIsDelete(byte b);
}