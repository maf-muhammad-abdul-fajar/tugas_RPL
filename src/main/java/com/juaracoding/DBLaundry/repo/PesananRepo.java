package com.juaracoding.DBLaundry.repo;

import com.juaracoding.DBLaundry.model.Pesanan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PesananRepo extends JpaRepository<Pesanan,Long> {
    List<Pesanan> findByIsDelete(byte byteIsDelete);
    Page<Pesanan> findByIsDelete(Pageable page,byte byteIsDelete);
    Page<Pesanan> findByIsDeleteAndPelangganNamaLengkapContainsIgnoreCase(Pageable page , byte byteIsDelete, String values);
    Page<Pesanan> findByIsDeleteAndPaketLayananHargaPerKilo(Pageable page , byte byteIsDelete, Double values);
    Page<Pesanan> findByIsDeleteAndIdPesanan(Pageable page , byte byteIsDelete, Long values);
    Page<Pesanan> findByIsDeleteAndBerat(Pageable page , byte byteIsDelete, Double values);
    Page<Pesanan> findByIsDeleteAndTotalHarga(Pageable page , byte byteIsDelete, Double values);
    Page<Pesanan> findByIsDeleteAndPaketLayananNamaPaketContainsIgnoreCase(Pageable page , byte byteIsDelete, String values);
    Page<Pesanan> findByIsDeleteAndPaketLayananTipeLayananContainsIgnoreCase(Pageable page , byte byteIsDelete, String values);
    Page<Pesanan> findByIsDeleteAndPembayaranNamaPembayaranContainsIgnoreCase(Pageable page , byte byteIsDelete, String values);
    List<Pesanan> findByIsDeleteAndPelangganNamaLengkapContainsIgnoreCase(byte byteIsDelete, String values);
    List<Pesanan> findByIsDeleteAndIdPesanan(byte byteIsDelete, Long values);

    @Query("SELECT SUM(a.berat * d.hargaPerKilo) FROM Pesanan a " +
            "JOIN a.pelanggan b " +
            "JOIN a.pembayaran c " +
            "JOIN a.paketLayanan d  WHERE DATENAME(month, GETDATE()) = DATENAME(month, a.createdDate) AND a.isDelete = 1")
    Double calculationCurrentMonthReport();

    @Query("SELECT a.pelanggan.namaLengkap,a.paketLayanan.namaPaket,a.paketLayanan.tipeLayanan,a.berat,a.paketLayanan.hargaPerKilo FROM Pesanan a " +
            "JOIN a.pelanggan b " +
            "JOIN a.pembayaran c " +
            "JOIN a.paketLayanan d  WHERE DATENAME(month, GETDATE()) = DATENAME(month, a.createdDate) AND a.isDelete = 1")
    List<Pesanan> pesananPerMonth();

    @Query("SELECT SUM(a.berat * d.hargaPerKilo) FROM Pesanan a " +
            "JOIN a.pelanggan b " +
            "JOIN a.pembayaran c " +
            "JOIN a.paketLayanan d  WHERE DATENAME(month, DATEADD(month,:numbr,GETDATE())) = DATENAME(month, a.createdDate) AND a.isDelete = 1")
    Double calculationDynamicReport(Double numbr);
}

