package com.juaracoding.DBLaundry.controller;/*
IntelliJ IDEA 2022.3.2 (Ultimate Edition)
Build #IU-223.8617.56, built on January 26, 2023
@Author User a.k.a. Safril Efendi Lubis
Java Developer
Created on 12/03/2023 13:27
@Last Modified 12/03/2023 13:27
Version 1.1
*/

import com.juaracoding.DBLaundry.configuration.OtherConfig;
import com.juaracoding.DBLaundry.dto.PaketLayananDTO;
import com.juaracoding.DBLaundry.model.PaketLayanan;
import com.juaracoding.DBLaundry.service.PaketLayananService;
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
public class PaketLayananContoller {

    private PaketLayananService paketLayananService;
    @Autowired
    private ModelMapper modelMapper;
    private Map<String,Object> objectMapper = new HashMap<String,Object>();
    private Map<String,String> mapSorting = new HashMap<String,String>();
    private List<PaketLayanan> lsCPUpload = new ArrayList<PaketLayanan>();
    private String [] strExceptionArr = new String[2];
    private MappingAttribute mappingAttribute = new MappingAttribute();

    @Autowired
    public PaketLayananContoller(PaketLayananService paketLayananService) {
        strExceptionArr[0] = "PaketLayananController";
        mapSorting();
        this.paketLayananService = paketLayananService;
    }

    private void mapSorting()
    {
        mapSorting.put("id","idListHarga");
        mapSorting.put("nama","namaPaket");
        mapSorting.put("harga","hargaPerKilo");
        mapSorting.put("tipe","tipeLayanan");
    }

    //API GET BERFUNGSI UNTUK MEMUNCULKAN MODALS CREATE PAKET LAYANAN
    @GetMapping("/v1/paketlayanan/new")
    public String createPaketLayanan(Model model, WebRequest request)
    {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model,objectMapper,request);
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        //memasukan atribut paketlayanan dari paketlayanan dto ke create_paketlayanan.html
        model.addAttribute("paketLayanan", new PaketLayananDTO());
        return "paket/create_paketlayanan";
    }

    //API GET BERFUNGSI UNTUK MEMUNCULKAN EDIT PAKET LAYANAN
    @GetMapping("/v1/paketlayanan/edit/{id}")
    public String editPaketLayanan(Model model, WebRequest request, @PathVariable("id") Long id)
    {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        //objectMapper mengambil data paketLayanan dengan findById(id,request)
        objectMapper = paketLayananService.findById(id,request);
        PaketLayananDTO paketLayananDTO = (objectMapper.get("data")==null?null:(PaketLayananDTO) objectMapper.get("data"));
        if((Boolean) objectMapper.get("success"))
        {
            //jika object mapper bernilai TRUE atau success maka akan masuk ke dalam edit_paketlayanan.html
            //membuat object paketlayananDTOForSelect sebagai penampung dari paketlayananDTO data
            //fungsi paketLayananDTO adalah mendapatkan data dari variabel objectmapper jika null maka akan berisi null dan jika tidak maka akan terisi data dari pakeLayanan
            //kemudian add attribute paketlayananDTO ke dalam edit_paketlayanan.html
            PaketLayananDTO paketLayananDTOForSelect = (PaketLayananDTO) objectMapper.get("data");
            model.addAttribute("paketLayanan", paketLayananDTO);
            return "paket/edit_paketlayanan";
        }
        else
        {
        //jika tidak success atau FALSE maka akan redirect ke menu halaman awal paketlayanan

            model.addAttribute("paketLayanan", new PaketLayananDTO());
            return "redirect:/api/mgmnt/v1/paketlayanan/default";
        }
    }

    //API POST BERFUNGSI UNTUK MENAMBAH DATA PAKET LAYANAN BARU
    @PostMapping("/v1/paketlayanan/new")
    public String newPaketLayanan(@ModelAttribute(value = "paketLayanan")
                            @Valid PaketLayananDTO paketLayananDTO
            , BindingResult bindingResult
            , Model model
            , WebRequest request
    )
    {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }

        /* START VALIDATION */
        //jika data binding terjadi error maka akan muncul status error
        if(bindingResult.hasErrors())
        {
            model.addAttribute("paketLayanan",paketLayananDTO);
            model.addAttribute("status","error");

            return "paket/create_paketlayanan";
        }

        //set isValid TRUE
        Boolean isValid = true;

        //jika isvalid FALSE maka akan muncul modal create_paketlayanan.html
        if(!isValid)
        {
            model.addAttribute("paketLayanan",paketLayananDTO);
            return "paket/create_paketlayanan";
        }
        /* END OF VALIDATION */

        //mengisi object paketLayanan dengan modelMapper dengan mapping paketLayananDTO beserta token
        //mengisi objectMapper dengan savePaketLayanan(paketLayanan,request)
        PaketLayanan paketLayanan = modelMapper.map(paketLayananDTO, new TypeToken<PaketLayanan>() {}.getType());
        objectMapper = paketLayananService.savePaketLayanan(paketLayanan,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            //jika objectMapper terjadi error saat save data maka akan redirect logout
            return "redirect:/api/check/logout";
        }

        if((Boolean) objectMapper.get("success"))
        {
        //jika objectMapper bernilai success atau TRUE maka data berhasil disimpan akan redirect ke halaman paketlayanan dan muncul message berhasil disimpan
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("message","DATA BERHASIL DISIMPAN");
            Long idDataSave = objectMapper.get("idDataSave")==null?1:Long.parseLong(objectMapper.get("idDataSave").toString());
            return "redirect:/api/mgmnt/v1/paketlayanan/fbpsb/0/asc/idListHarga?columnFirst=id&valueFirst="+idDataSave+"&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        }
        //jika mapper bernilai false maka akan masuk ke create_paketlayanan.html
        else
        {
            //memasukan data error message ke dalam mapping attribute dan set attribute model error
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("paketLayanan",new PaketLayananDTO());
            model.addAttribute("status","error");
            return "paket/create_paketlayanan";
        }
    }

    //API POST BERFUNGSI UNTUK MENGEDIT PAKET LAYANAN
    @PostMapping("/v1/paketlayanan/edit/{id}")
    public String editPaketLayanan(@ModelAttribute("paketLayanan")
                             @Valid PaketLayananDTO paketLayananDTO
            , BindingResult bindingResult
            , Model model
            , WebRequest request
            , @PathVariable("id") Long id
    )
    {
        /* START VALIDATION */
        //jika data binding error maka akan return ke modal edit_paketlayanan.html
        if(bindingResult.hasErrors())
        {
            model.addAttribute("paketLayanan",paketLayananDTO);
            return "paket/edit_paketlayanan";
        }
        Boolean isValid = true;

        //jika isValid false maka akan return ke dalam edit_paketlayanan.html
        if(!isValid)
        {
            model.addAttribute("paketLayanan",paketLayananDTO);
            return "paket/edit_paketlayanan";
        }
        /* END OF VALIDATION */

        //membuat object paketlayanan dan mapping dengan paketlayananDTO beserta token
        //mengisi objectMapper dengan updatePaketLayanan(id,paketLayanan,request)
        PaketLayanan paketLayanan = modelMapper.map(paketLayananDTO, new TypeToken<PaketLayanan>() {}.getType());
        objectMapper = paketLayananService.updatePaketLayanan(id,paketLayanan,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            return "redirect:/api/check/logout";
        }

        //jika object mapper bernilai success atau true maka data berhasil disimpan dan akan di redirect ke halaman paketlayanan
        if((Boolean) objectMapper.get("success"))
        {
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("paketLayanan",new PaketLayananDTO());
            return "redirect:/api/mgmnt/v1/paketlayanan/fbpsb/0/asc/idListHarga?columnFirst=id&valueFirst="+id+"&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        }
        //jika mapper bernilai false maka akan masuk ke edit_paketlayanan.html
        else
        {
            //memasukan data error message ke dalam mapping attribute dan set attribute model error
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("paketLayanan",new PaketLayananDTO());
            return "paket/edit_paketlayanan";
        }
    }

    //API GET BERFUNGSI UNTUK MENAMPILKAN MENU PAKET LAYANAN
    @GetMapping("/v1/paketlayanan/default")
    public String getDefaultData(Model model,WebRequest request)
    {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        //membuat object pageabel dan mengisi pagerequest(page,size,properties)
        //mengisi objectmapper dengan findallpelanggan dengan parameter pageable dan request
        Pageable pageable = PageRequest.of(0,5, Sort.by("idListHarga"));
        objectMapper = paketLayananService.findAllPaketLayanan(pageable,request);
        mappingAttribute.setAttribute(model,objectMapper,request);

        //masuk ke paketlayanan.html
        model.addAttribute("paketLayanan",new PaketLayananDTO());
        model.addAttribute("sortBy","idListHarga");
        model.addAttribute("currentPage",1);
        model.addAttribute("asc","asc");
        model.addAttribute("columnFirst","");
        model.addAttribute("valueFirst","");
        model.addAttribute("sizeComponent",5);
        return "paket/paketlayanan";
    }

    //API GET BERFUNGSI UNTUK SORTING MENU PAKET LAYANAN
    @GetMapping("/v1/paketlayanan/fbpsb/{page}/{sort}/{sortby}")
    public String findByPaketLayanan(
            Model model,
            @PathVariable("page") Integer pagez,
            @PathVariable("sort") String sortz,
            @PathVariable("sortby") String sortzBy,
            @RequestParam String columnFirst,
            @RequestParam String valueFirst,
            @RequestParam String sizeComponent,
            WebRequest request
    ){
        //mengambil value dari request
        //isi object pageable dengan page sort dengan parameter di dalamnya dengan parameter diatas
        sortzBy = mapSorting.get(sortzBy);
        sortzBy = sortzBy==null?"idListHarga":sortzBy;
        Pageable pageable = PageRequest.of(pagez==0?pagez:pagez-1,Integer.parseInt(sizeComponent.equals("")?"5":sizeComponent), sortz.equals("asc")?Sort.by(sortzBy):Sort.by(sortzBy).descending());
        objectMapper = paketLayananService.findByPage(pageable,request,columnFirst,valueFirst);
        mappingAttribute.setAttribute(model,objectMapper,request);
        model.addAttribute("paketLayanan",new PaketLayananDTO());
        model.addAttribute("currentPage",pagez==0?1:pagez);
        model.addAttribute("sortBy", ManipulationMap.getKeyFromValue(mapSorting,sortzBy));
        model.addAttribute("columnFirst",columnFirst);
        model.addAttribute("valueFirst",valueFirst);
        model.addAttribute("sizeComponent",sizeComponent);

        return "paket/paketlayanan";
    }

    //API GET BERFUNGSI UNTUK MENGHAPUS PAKET LAYANAN
    @GetMapping("/v1/paketlayanan/delete/{id}")
    public String deletePaketLayanan(Model model
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
        //mengisi objectmapper dengan paketlayananervice delete paketlayanan dengan parameter id dan request
        objectMapper = paketLayananService.deletePaketLayanan((long) id,request);
        if (objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            //jika error maka akan redirect ke logout
            return "redirect:/api/check/logout";
        }

        //jika objectmapper bernilai TRUE atau success maka akan redirect ke tampilan paketlayanan.html
        if ((Boolean) objectMapper.get("success")) {
            mappingAttribute.setAttribute(model, objectMapper);
            model.addAttribute("paket", new PaketLayananDTO());
            return "redirect:/api/mgmnt/v1/paketlayanan/fbpsb/0/asc/idListHarga?columnFirst=idListHarga&valueFirst=" + id + "&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        } else {
            //jika FALSE maka akan masuk ke paketlayanan.html
            mappingAttribute.setAttribute(model, objectMapper);
            model.addAttribute("paket", new PaketLayananDTO());
            return "paket/paketlayanan";
        }
    }
}
