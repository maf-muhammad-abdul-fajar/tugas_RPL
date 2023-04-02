package com.juaracoding.DBLaundry.controller;/*
Created By IntelliJ IDEA 2022.2.3 (Community Edition)
IntelliJ IDEA 2022.3.2 (Community Edition)
Build #IC-223.8617.56, built on January 26, 2023
@Author USER a.k.a. Deby Utari
Java Developer
Created on 12/03/2023 16:08
@Last Modified 12/03/2023 16:08
Version 1.0
*/

import com.juaracoding.DBLaundry.configuration.OtherConfig;
import com.juaracoding.DBLaundry.dto.PelangganDTO;
import com.juaracoding.DBLaundry.model.Pelanggan;
import com.juaracoding.DBLaundry.service.PelangganService;
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
public class PelangganController {

    private PelangganService pelangganService;
    @Autowired
    private ModelMapper modelMapper;
    private Map<String,Object> objectMapper = new HashMap<String,Object>();
    private Map<String,String> mapSorting = new HashMap<String,String>();
    private List<Pelanggan> lsCPUpload = new ArrayList<Pelanggan>();
    private String [] strExceptionArr = new String[2];
    private MappingAttribute mappingAttribute = new MappingAttribute();

    @Autowired
    public PelangganController(PelangganService pelangganService) {
        strExceptionArr[0] = "PelangganController";
        mapSorting();
        this.pelangganService = pelangganService;
    }

    private void mapSorting()
    {
        mapSorting.put("id","idPelanggan");
        mapSorting.put("nama","namaLengkap");
        mapSorting.put("alamat","alamatLengkap");
        mapSorting.put("no","noHandphone");
    }

    //API GET BERFUNGSI UNTUK MEMUNCULKAN MODAL CREATE PAKET LAYANAN
    @GetMapping("/v1/pelanggan/new" )
    public String createPelanggan(Model model, WebRequest request)
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
        //memasukan atribut paketlayanan dari paketlayanan dto ke create_paketlayanan.html
        model.addAttribute("pelanggan", new PelangganDTO());
        return "pelanggan/create_pelanggan";
    }

    //API GET BERFUNGSI UNTUK MENAMPILKAN MODAL EDIT PAKET LAYANAN
    @GetMapping("/v1/pelanggan/edit/{id}")
    public String editPelanggan(Model model, WebRequest request, @PathVariable("id") Long id)
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
        //objectMapper mengambil data pelanggan dengan findById(id,request)
        objectMapper = pelangganService.findById(id,request);
        PelangganDTO pelangganDTO = (objectMapper.get("data")==null?null:(PelangganDTO) objectMapper.get("data"));
        if((Boolean) objectMapper.get("success"))
        {
            //jika object mapper bernilai TRUE atau success maka akan muncul modal edit_pelanggan.html
            //membuat object pelangganDTOForSelect sebagai penampung dari paketlayananDTO data
            //fungsi pelangganDTO adalah mendapatkan data dari variabel objectmapper jika null maka akan berisi null dan jika tidak maka akan terisi data dari pelanggan
            //kemudian add attribute pelangganDTO ke dalam edit_paketlayanan.html
            PelangganDTO pelangganDTOForSelect = (PelangganDTO) objectMapper.get("data");
            model.addAttribute("pelanggan", pelangganDTO);
            return "pelanggan/edit_pelanggan";
        }
        else
        {
        //jika tidak success atau FALSE maka akan redirect ke menu halaman awal pelanggan
            model.addAttribute("pelanggan", new PelangganDTO());
            return "redirect:/api/mgmnt/v1/pelanggan/default";
        }
    }

    //API POST BERFUNGSI UNTUK MENAMBAHKAN DATA PELANGGAN BARU
    @PostMapping("/v1/pelanggan/new")
    public String newPelanggan(@ModelAttribute(value = "pelanggan")
                            @Valid PelangganDTO pelangganDTO
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
            model.addAttribute("pelanggan",pelangganDTO);
            model.addAttribute("status","error");

            return "pelanggan/create_pelanggan";
        }

        //set isValid TRUE
        Boolean isValid = true;

        //jika isValid FALSE maka akan muncul modal create_pelanggan.html
        if(!isValid)
        {
            model.addAttribute("pelanggan",pelangganDTO);
            return "pelanggan/create_pelanggan";
        }
        /* END OF VALIDATION */

        //mengisi object pelanggan dengan modelMapper dengan mapping pelangganDTO beserta token
        //mengisi objectMapper dengan savePelanggan(pelanggan,request)
        Pelanggan pelanggan = modelMapper.map(pelangganDTO, new TypeToken<Pelanggan>() {}.getType());
        objectMapper = pelangganService.savePelanggan(pelanggan,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            //jika objectMapper terjadi error saat save data maka akan redirect logout
            return "redirect:/api/check/logout";
        }
        if((Boolean) objectMapper.get("success"))
        {
        //jika objectMapper berhasil atau TRUE maka akan redirect ke halaman menu pelanggan dan muncul message data berhasil disimpan
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("message","DATA BERHASIL DISIMPAN");
            Long idDataSave = objectMapper.get("idDataSave")==null?1:Long.parseLong(objectMapper.get("idDataSave").toString());
            return "redirect:/api/mgmnt/v1/pelanggan/fbpsb/0/asc/idpelanggan?columnFirst=id&valueFirst="+idDataSave+"&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        }
        //jika object mapper bernilai false maka akan muncul error
        else
        {
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("pelanggan",new PelangganDTO());
            model.addAttribute("status","error");
            return "pelanggan/create_pelanggan";
        }
    }

    //API POST BERFUNGSI UNTUK MENGEDIT DATA PELANGGAN
    @PostMapping("/v1/pelanggan/edit/{id}")
    public String editPelanggan(@ModelAttribute("pelanggan")
                             @Valid PelangganDTO pelangganDTO
            , BindingResult bindingResult
            , Model model
            , WebRequest request
            , @PathVariable("id") Long id
    )
    {
        /* START VALIDATION */
        //jika data binding error maka akan return ke modal edit_pelanggan.html
        if(bindingResult.hasErrors())
        {
            model.addAttribute("pelanggan",pelangganDTO);
            return "pelanggan/edit_pelanggan";
        }
        Boolean isValid = true;

        //jika isValid false maka akan return ke edit_pelanggan.html
        if(!isValid)
        {
            model.addAttribute("pelanggan",pelangganDTO);
            return "pelanggan/edit_pelanggan";
        }
        /* END OF VALIDATION */
        //membuat object pelanggan dan mapping dengan pelangganDTO beserta token
        //mengisi objectMapper dengan updatePelanggan(id,pelanggan,request)
        Pelanggan pelanggan = modelMapper.map(pelangganDTO, new TypeToken<Pelanggan>() {}.getType());
        objectMapper = pelangganService.updatePelanggan(id,pelanggan,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            //jika terjadi error saat update pelanggan maka akan redirect logout
            return "redirect:/api/check/logout";
        }

        //jika objectmapper bernilai true atau berhasil maka akan redirect ke tampilan awal pelanggan
        if((Boolean) objectMapper.get("success"))
        {
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("pelanggan",new PelangganDTO());
            return "redirect:/api/mgmnt/v1/pelanggan/fbpsb/0/asc/idPelanggan?columnFirst=id&valueFirst="+id+"&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        }
        //jika objectmapper gagal maka akan muncul error
        else
        {
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("pelanggan",new PelangganDTO());
            return "pelanggan/edit_pelanggan";
        }
    }

    //API GET BERFUNGSI UNTUK MENAMPILKAN HALAMAN AWAL PELANGGAN
    @GetMapping("/v1/pelanggan/default")
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

        //membuat object pageable dan mendapatkan pagerequest dengan parameter page,size,dan sort by properties
        //mengisi objectmapper dengan findallpelanggan dengan parameter pageable dan request
        Pageable pageable = PageRequest.of(0,5, Sort.by("idPelanggan"));
        objectMapper = pelangganService.findAllPelanggan(pageable,request);
        mappingAttribute.setAttribute(model,objectMapper,request);

        //masuk ke pelanggan.html
        model.addAttribute("pelanggan",new PelangganDTO());
        model.addAttribute("sortBy","idPelanggan");
        model.addAttribute("currentPage",1);
        model.addAttribute("asc","asc");
        model.addAttribute("columnFirst","");
        model.addAttribute("valueFirst","");
        model.addAttribute("sizeComponent",5);
        return "/pelanggan/pelanggan";
    }

    //API GET BERFUNGSI UNTUK SORTING DATA PELANGGAN
    @GetMapping("/v1/pelanggan/fbpsb/{page}/{sort}/{sortby}")
    public String findByPelanggan(
            Model model,
            @PathVariable("page") Integer pagez,
            @PathVariable("sort") String sortz,
            @PathVariable("sortby") String sortzBy,
            @RequestParam String columnFirst,
            @RequestParam String valueFirst,
            @RequestParam String sizeComponent,
            WebRequest request
    ){
        //mengambil value dari request param
        //isi object pageable dengan pagerequest dengan parameter dari request param
        sortzBy = mapSorting.get(sortzBy);
        sortzBy = sortzBy==null?"idPelanggan":sortzBy;
        Pageable pageable = PageRequest.of(pagez==0?pagez:pagez-1,Integer.parseInt(sizeComponent.equals("")?"5":sizeComponent), sortz.equals("asc")?Sort.by(sortzBy):Sort.by(sortzBy).descending());
        objectMapper = pelangganService.findByPage(pageable,request,columnFirst,valueFirst);
        mappingAttribute.setAttribute(model,objectMapper,request);
        model.addAttribute("pelanggan",new PelangganDTO());
        model.addAttribute("currentPage",pagez==0?1:pagez);
        model.addAttribute("sortBy", ManipulationMap.getKeyFromValue(mapSorting,sortzBy));
        model.addAttribute("columnFirst",columnFirst);
        model.addAttribute("valueFirst",valueFirst);
        model.addAttribute("sizeComponent",sizeComponent);

        //masuk ke pelanggan.html
        return "/pelanggan/pelanggan";
    }

    //API GET BERFUNGSI UNTUK MENGHAPUS DATA PELANGGAN
    @GetMapping("/v1/pelanggan/delete/{id}")
    public String deletePelanggan(Model model, WebRequest request, @PathVariable("id") Long id)
    {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        //mengisi objectMapper dengan method dari pelanggan service dengan deletePelanggan(id,request)
        //set attribute model dan objectMapper
        //lalu redirect ke pelanggan
        objectMapper = pelangganService.deletePelanggan(id,request);
        mappingAttribute.setAttribute(model,objectMapper);//untuk set session
        model.addAttribute("pelanggan", new PelangganDTO());
        return "redirect:/api/mgmnt/v1/pelanggan/default";
    }
}
