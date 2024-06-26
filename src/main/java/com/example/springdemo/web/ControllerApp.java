package com.example.springdemo.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.springdemo.entities.Accessory;
import com.example.springdemo.entities.AppRole;
import com.example.springdemo.entities.AppUser;
import com.example.springdemo.entities.Category;
import com.example.springdemo.repository.RepoAccessory;
import com.example.springdemo.repository.RepoCategory;
import com.example.springdemo.repository.RepoRole;
import com.example.springdemo.service.AppService;

import jakarta.validation.Valid;

@Controller
public class ControllerApp {

    @Autowired
    RepoAccessory repoAccessory;

    @Autowired
    RepoCategory repoCategory;

    @Autowired
    RepoRole repoRole;

    @Autowired
    private AppService appService;

    @GetMapping("/home")
    public String pageHome(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "4") int size,
            @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Accessory> accessory = repoAccessory.findByBrandContains(keyword, pageable);
        model.addAttribute("listAccessory", accessory);
        model.addAttribute("pages", new int[accessory.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        return "home";
    }

    @GetMapping("/notAuthorized")
    public String notAuthorizedPage() {
        return "notAuthorized";
    }

    /************** Accessory ***************** */

    @GetMapping("/accessory/add")
    public String pageAccessory(Model model) {
        model.addAttribute("newAccessory", new Accessory());
        model.addAttribute("listCategory", repoCategory.findAll());
        return "addAccessory";
    }

    @PostMapping("/accessory/save")
    public String saveAccessory(Model model,
            @Valid @ModelAttribute("newAccessory") Accessory newAccessory,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("listCategory", repoCategory.findAll());
            return "addAccessory";
        }
        repoAccessory.save(newAccessory);
        redirectAttributes.addFlashAttribute("successMessage", "Accessory added successfully");
        return "redirect:/accessory/add";
    }

    /************** Category ***************** */

    @GetMapping("/category/add")
    public String pageCategory(Model model) {
        model.addAttribute("newCategory", new Category());
        return "addCategory";
    }

    @PostMapping("/category/save")
    public String addNewCategory(@ModelAttribute("newCategory") @Valid Category category,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "addCategory";
        }
        repoCategory.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Accessory added successfully");
        return "redirect:/category/add";
    }

    /************** Delete Accessory ***************** */

    @GetMapping("/accessory/delete")
    public String deleteAcc(Long id, String keyword, int page, RedirectAttributes redirectAttributes) {
        repoAccessory.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Accessory deleted successfully");
        return "redirect:/home?keyword=" + keyword + "&page=" + page;
    }

    /************** Update Accessory ***************** */

    @GetMapping("/accessory/update")
    public String updateAccessory(Long id, Model model, String keyword, int page) {
        Accessory acc = repoAccessory.findById(id).get();
        List<Category> categories = repoCategory.findAll();
        model.addAttribute("accessory", acc);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        return "formUpdateAcc";
    }

    @PostMapping("/accessory/update/save")
    public String updating(Model model,
            @ModelAttribute Accessory accessory,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page) {

        repoAccessory.save(accessory);
        return "redirect:/home?keyword=" + keyword + "&page=" + page;
    }

    /**************** Add Users ***************** */

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add/users")
    public String addUsers(Model model) {
        model.addAttribute("newUser", new AppUser());
        model.addAttribute("roles", repoRole.findAll());
        return "addUsers";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/save/user")
    public String saveUser(@Valid @ModelAttribute("newUser") AppUser user,
            @ModelAttribute("roles") AppRole role, BindingResult result,
            @RequestParam(name = "password-confirm") String confPassword,
            RedirectAttributes redirectAttributes, Model model) {

        if (result.hasErrors()) {
            return "addUsers";
        }

        AppUser newUser = appService.addUser(
                user.getUsername(), user.getPassword(), confPassword);
        appService.addRoleToUser(newUser.getUsername(), role.getRole());
        redirectAttributes.addFlashAttribute("successMessage",
                "success The user" + " " + newUser.getUsername() + " was added successfully !");
        // model.addAttribute("errorMsg", e.getMessage());
        return "redirect:/add/users";

    }

}