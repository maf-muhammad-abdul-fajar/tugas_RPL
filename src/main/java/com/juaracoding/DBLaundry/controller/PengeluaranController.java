package com.juaracoding.DBLaundry.controller;/*
Created By IntelliJ IDEA 2022.3.2 (Ultimate Edition)
Build #IU-223.8617.56, built on January 26, 2023
@Author Syarifudin a.k.a. Muhamad Syarifudin
Java Developer
Created on 09/03/2023 12:57
@Last Modified 09/03/2023 12:57
Version 1.1
*/

import com.juaracoding.DBLaundry.configuration.OtherConfig;
import com.juaracoding.DBLaundry.dto.PengeluaranDTO;
import com.juaracoding.DBLaundry.model.Pengeluaran;
import com.juaracoding.DBLaundry.service.PengeluaranService;
import com.juaracoding.DBLaundry.utils.ConstantMessage;
import com.juaracoding.DBLaundry.utils.ManipulationMap;
import com.juaracoding.DBLaundry.utils.MappingAttribute;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/mgmnt")
public class PengeluaranController {

    private PengeluaranService pengeluaranService;
    @Autowired
    private ModelMapper modelMapper;
    private Map<String, Object> objectMapper = new HashMap<String, Object>();
    private Map<String, String> mapSorting = new HashMap<String, String>();
    private List<Pengeluaran> lsCPUpload = new ArrayList<Pengeluaran>();
    private String[] strExceptionArr = new String[2];
    private MappingAttribute mappingAttribute = new MappingAttribute();

    @Autowired
    public PengeluaranController(PengeluaranService pengeluaranService) {
        strExceptionArr[0] = "PengeluaranController";
        mapSorting();
        this.pengeluaranService = pengeluaranService;
    }

    private void mapSorting() {
        mapSorting.put("id", "idPengeluaran");
        mapSorting.put("nama", "namaPengeluaran");
        mapSorting.put("biaya", "biaya");
    }

    //API GET BERFUNGSI UNTUK MENAMPILKAN MODAL CREATE PENGELUARAN
    @GetMapping("/v1/pengeluaran/new")
    public String createPengeluaran(Model model, WebRequest request) {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if (OtherConfig.getFlagSessionValidation().equals("y")) {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
            if (request.getAttribute("USR_ID", 1) == null) {
                return "redirect:/api/check/logout";
            }
        }
        //memasukan attribute pengeluaran ke create_pengeluaran.html
        model.addAttribute("pengeluaran", new PengeluaranDTO());
        return "pengeluaran/create_pengeluaran";
    }

    //API GET BERFUNGSI UNTUK MENAMPILKAN MODAL EDIT PENGELUARAN
    @GetMapping("/v1/pengeluaran/edit/{id}")
    public String editPengeluaran(Model model, WebRequest request, @PathVariable("id") Long id) {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if (OtherConfig.getFlagSessionValidation().equals("y")) {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
            if (request.getAttribute("USR_ID", 1) == null) {
                return "redirect:/api/check/logout";
            }
        }

        //objectMapper mengambil data pengeluaran dengan findById(id,request)
        //pengeluaranDTO akan terisi jika data tidak null maka berisi PengeluaranDTO object
        objectMapper = pengeluaranService.findById(id, request);
        PengeluaranDTO pengeluaranDTO = (objectMapper.get("data") == null ? null : (PengeluaranDTO) objectMapper.get("data"));
        //jika objectMapper bernilai TRUE atau success
        if ((Boolean) objectMapper.get("success")) {
            //pengeluaranDTOForSelect berisi pengeluaranDTO object data
            //add attribute pengeluaran=pengeluaranDTO ke edit_pengeluaran.html
            PengeluaranDTO pengeluaranDTOForSelect = (PengeluaranDTO) objectMapper.get("data");
            model.addAttribute("pengeluaran", pengeluaranDTO);
            return "pengeluaran/edit_pengeluaran";

        } else {
            //jika objectMapper bernilai FALSE
            //add attribute pengeluaran=pengeluaranDTO to pengeluaran.html
            model.addAttribute("pengeluaran", new PengeluaranDTO());
            return "redirect:/api/mgmnt/pengeluaran/default";
        }
    }

    //API POST BERFUNGSI UNTUK MENAMBAH DATA PENGELUARAN
    @PostMapping("/v1/pengeluaran/new")
    public String newPengeluaran(@ModelAttribute(value = "pengeluaran")
                                 @Valid PengeluaranDTO pengeluaranDTO
            , BindingResult bindingResult
            , Model model
            , WebRequest request
    ) {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if (OtherConfig.getFlagSessionValidation().equals("y")) {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
            if (request.getAttribute("USR_ID", 1) == null) {
                return "redirect:/api/check/logout";
            }
        }

        /* START VALIDATION */
        //jika data binding error
        if (bindingResult.hasErrors()) {
            //add attribute pengeluaran=pengeluaranDTO,status=error ke create_pengeluaran.html
            model.addAttribute("pengeluaran", pengeluaranDTO);
            model.addAttribute("status", "error");
            //menampilkan modal create_pengeluaran.html
            return "pengeluaran/create_pengeluaran";
        }

        //set isValid bernilai TRUE
        Boolean isValid = true;

        //jika isValid bernilai FALSE
        if (!isValid) {
            //add attribute pengeluaran=pengeluaranDTO to create_pengeluaran.html
            model.addAttribute("pengeluaran", pengeluaranDTO);
            //menampilkan modal create_pengeluaran.html
            return "pengeluaran/create_pengeluaran";
        }
        /* END OF VALIDATION */

        //isi object pengeluaran dengan modelMapper mapping data pengeluaranDTO beserta token
        //objectMapper menampung dan menjalankan function savePengeluaran(pengeluaran,request)
        Pengeluaran pengeluaran = modelMapper.map(pengeluaranDTO, new TypeToken<Pengeluaran>() {
        }.getType());
        objectMapper = pengeluaranService.savePengeluaran(pengeluaran, request);
        if (objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
        //jika objectMapper message bernilai error
            //redirect logout
            return "redirect:/api/check/logout";
        }

        //jika objectMapper bernilai TRUE atau success
        if ((Boolean) objectMapper.get("success")) {
            //set mappingAttribute model objectMapper
            //addAttribute message to redirect pengeluaran.html
            //object idDataSave berisi data idDataSave dari objectMapper, jika bernilai null maka value 1, jika tidak maka get idDataSave
            //redirect pengeluaran.html
            mappingAttribute.setAttribute(model, objectMapper);
            model.addAttribute("message", "DATA BERHASIL DISIMPAN");
            Long idDataSave = objectMapper.get("idDataSave") == null ? 1 : Long.parseLong(objectMapper.get("idDataSave").toString());
            return "redirect:/api/mgmnt/v1/pengeluaran/fbpsb/0/asc/idPengeluaran?columnFirst=idPengeluaran&valueFirst=" + idDataSave + "&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        } else {
            //jika objectMapper bernilai FALSE
            //set error message to attribute
            //add attribute pengeluaran=new pengeluaranDTO, dan status=error from mappingAttribute
            //menampilkan create_pengeluaran modal
            mappingAttribute.setErrorMessage(bindingResult, objectMapper.get("message").toString());
            model.addAttribute("pengeluaran", new PengeluaranDTO());
            model.addAttribute("status", "error");
            return "pengeluaran/create_pengeluaran";
        }
    }

    //API POST BERFUNGSI UNTUK MENGEDIT DATA PENGELUARAN
    @PostMapping("/v1/pengeluaran/edit/{id}")
    public String doRegis(@ModelAttribute("pengeluaran")
                          @Valid PengeluaranDTO pengeluaranDTO
            , BindingResult bindingResult
            , Model model
            , WebRequest request
            , @PathVariable("id") Long id
    ) {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if (OtherConfig.getFlagSessionValidation().equals("y")) {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
            if (request.getAttribute("USR_ID", 1) == null) {
                return "redirect:/api/check/logout";
            }
        }
        /* START VALIDATION */
        //jika data binding terjadi error
        if (bindingResult.hasErrors()) {
            //add attribute to model pengeluaran=pengeluaranDTP
            //menampilkan edit_pengeluaran modal
            model.addAttribute("pengeluaran", pengeluaranDTO);
            return "pengeluaran/edit_pengeluaran";
        }

        //set isValid bernilai TRUE
        Boolean isValid = true;

        //jika isValid bernilai FALSE
        if (!isValid) {
            //add attribute to model pengeluaran=pengeluaranDTO
            //menampilkan edit_pengeluaran.html
            model.addAttribute("pengeluaran", pengeluaranDTO);
            return "pengeluaran/edit_pengeluaran";
        }
        /* END OF VALIDATION */

        //membuat object pengeluaran berisi modelMapper dengan mapping data pengeluarabDTO beserta token
        //objectMapper menampung proses updatePengeluaran(id,pengeluaran,request)
        Pengeluaran pengeluaran = modelMapper.map(pengeluaranDTO, new TypeToken<Pengeluaran>() {
        }.getType());
        objectMapper = pengeluaranService.updatePengeluaran(id, pengeluaran, request);
        if (objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            //jika object mapper message adalah error maka redirect logout
            return "redirect:/api/check/logout";
        }
        //jika objectMapper bernilai TRUE atau success
        if ((Boolean) objectMapper.get("success")) {
            //set attribute model,objectMapper to mapping attribute
            //add attribute pengeluaran=pengeluaranDTO
            //redirect ke pengeluaran.html
            mappingAttribute.setAttribute(model, objectMapper);
            model.addAttribute("pengeluaran", new PengeluaranDTO());
            return "redirect:/api/mgmnt/v1/pengeluaran/fbpsb/0/asc/idPengeluaran?columnFirst=idPengeluaran&valueFirst=" + id + "&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        } else {
            //jika objectMapper bernilai FALSE
            //set error message to mappingAttribute
            //add attribute pengeluaran=pengeluaranDTO
            //menampilkan edit_pengeluaran.html modal
            mappingAttribute.setErrorMessage(bindingResult, objectMapper.get("message").toString());
            model.addAttribute("pengeluaran", new PengeluaranDTO());
            return "pengeluaran/edit_pengeluaran";
        }
    }

    //API GET BERFUNGSI UNTUK MENAMPILKAN HALAMAN AWAL PENGELUARAN
    @GetMapping("/v1/pengeluaran/default")
    public String getDefaultData(Model model, WebRequest request){
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if (OtherConfig.getFlagSessionValidation().equals("y")) {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
            if (request.getAttribute("USR_ID", 1) == null) {
                return "redirect:/api/check/logout";
            }
        }
        //object pageable menampung pagerequest dengan parameter page,size,sort=idPengeluaran
        //objectMapper menampung proses findAllPengeluaran(pageable,request)
        //set attribute model,objectMapper,request ke mappingAttribute
        Pageable pageable = PageRequest.of(0, 5, Sort.by("idPengeluaran"));
        objectMapper = pengeluaranService.findAllPengeluaran(pageable, request);
        mappingAttribute.setAttribute(model, objectMapper, request);
        //add attribute to pengeluaran.html
        model.addAttribute("pengeluaran", new PengeluaranDTO());
        model.addAttribute("sortBy", "idPengeluaran");
        model.addAttribute("currentPage", 1);
        model.addAttribute("asc", "asc");
        model.addAttribute("columnFirst", "");
        model.addAttribute("valueFirst", "");
        model.addAttribute("sizeComponent", 5);
        return "/pengeluaran/pengeluaran";
    }

    //API GET BERFUNGSI UNTUK MENSORTING PENGELUARAN
    @GetMapping("/v1/pengeluaran/fbpsb/{page}/{sort}/{sortby}")
    public String findByPengeluaran(
            Model model,
            @PathVariable("page") Integer pagez,
            @PathVariable("sort") String sortz,
            @PathVariable("sortby") String sortzBy,
            @RequestParam String columnFirst,
            @RequestParam String valueFirst,
            @RequestParam String sizeComponent,
            WebRequest request
    ) {
        //menampung value dari request param ke dalam variabel
        //object pageable menampung pagerequest dengan parameter dari variabel
        //objectMapper menampung proses findByPage(pageable,request,columnFirst,valueFirst)
        //setAttribute to mappingAttribute
        //add attribute to pengeluaran.html
        sortzBy = mapSorting.get(sortzBy);
        sortzBy = sortzBy == null ? "idPengeluaran" : sortzBy;
        Pageable pageable = PageRequest.of(pagez == 0 ? pagez : pagez - 1, Integer.parseInt(sizeComponent.equals("") ? "5" : sizeComponent), sortz.equals("asc") ? Sort.by(sortzBy) : Sort.by(sortzBy).descending());
        objectMapper = pengeluaranService.findByPage(pageable, request, columnFirst, valueFirst);
        mappingAttribute.setAttribute(model, objectMapper, request);
        model.addAttribute("pengeluaran", new PengeluaranDTO());
        model.addAttribute("currentPage", pagez == 0 ? 1 : pagez);
        model.addAttribute("sortBy", ManipulationMap.getKeyFromValue(mapSorting, sortzBy));
        model.addAttribute("columnFirst", columnFirst);
        model.addAttribute("valueFirst", valueFirst);
        model.addAttribute("sizeComponent", sizeComponent);

        return "/pengeluaran/pengeluaran";
    }

    @GetMapping("/v1/pengeluaran/delete/{id}")
    public String deletePengeluaran(Model model
            , WebRequest request
            , @PathVariable("id") byte id
    ) {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if (OtherConfig.getFlagSessionValidation().equals("y")) {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
            if (request.getAttribute("USR_ID", 1) == null) {
                return "redirect:/api/check/logout";
            }
        }

        //objectMapper menampung proses deletePengeluaran(id,request)
        objectMapper = pengeluaranService.deletePengeluaran(id,request);
        if (objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            //jika objectMapper message bernilai error maka redirect logout
            return "redirect:/api/check/logout";
        }
        //jika objectMapper bernilai success TRUE
        if ((Boolean) objectMapper.get("success")) {
            //setAttribute model,object to mappingAttribute
            //redirect ke halaman pengeluaran.html
            mappingAttribute.setAttribute(model, objectMapper);
            model.addAttribute("pengeluaran", new PengeluaranDTO());
            return "redirect:/api/mgmnt/v1/pengeluaran/fbpsb/0/asc/idPengeluaran?columnFirst=idPengeluaran&valueFirst=" + id + "&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        } else {
            //jika objectMapper bernilai FALSE
            //setAttribute mdoel,objectMapper to mappingAttribute
            //return ke pengeluaran.html
            mappingAttribute.setAttribute(model, objectMapper);
            model.addAttribute("pengeluaran", new PengeluaranDTO());
            return "pengeluaran/pengeluaran";
        }
    }
}