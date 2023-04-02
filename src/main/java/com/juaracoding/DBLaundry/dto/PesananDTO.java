package com.juaracoding.DBLaundry.dto;

import com.juaracoding.DBLaundry.model.PaketLayanan;
import com.juaracoding.DBLaundry.model.Pelanggan;
import com.juaracoding.DBLaundry.model.Pembayaran;

public class PesananDTO {

    private Long idPesanan;

    private PaketLayanan paketLayanan;

    private Pelanggan pelanggan;

    private Pembayaran pembayaran;

    private Double berat;

    private Long totalHarga;

    public Long getIdPesanan() {
        return idPesanan;
    }

    public void setIdPesanan(Long idPesanan) {
        this.idPesanan = idPesanan;
    }

    public PaketLayanan getPaketLayanan() {
        return paketLayanan;
    }

    public void setPaketLayanan(PaketLayanan paketLayanan) {
        this.paketLayanan = paketLayanan;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public void setPelanggan(Pelanggan pelanggan) {
        this.pelanggan = pelanggan;
    }

    public Pembayaran getPembayaran() {
        return pembayaran;
    }

    public void setPembayaran(Pembayaran pembayaran) {
        this.pembayaran = pembayaran;
    }

    public Double getBerat() {
        return berat;
    }

    public void setBerat(Double berat) {
        this.berat = berat;
    }

    public Long getTotalHarga() {
        return totalHarga;
    }

    public void setTotalHarga(Long totalHarga) {
        this.totalHarga = totalHarga;
    }
}
