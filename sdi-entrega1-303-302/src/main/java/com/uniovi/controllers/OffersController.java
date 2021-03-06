package com.uniovi.controllers;

import java.security.Principal;
import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.uniovi.entities.Offer;
import com.uniovi.entities.User;
import com.uniovi.services.OffersService;
import com.uniovi.services.UsersService;
import com.uniovi.validators.OfferAddFormValidator;
import com.uniovi.validators.OfferEditFormValidator;

@Controller
public class OffersController {
	@Autowired
	private UsersService usersService;
	@Autowired // Inyectar el servicio
	private OffersService offersService;
	@Autowired
	private OfferAddFormValidator offerAddFormValidator;
	@Autowired
	private OfferEditFormValidator offerEditFormValidator;

	@RequestMapping("/offer/list/update")
	public String updateList(Model model, Pageable pageable, Principal principal) {
		String Email = principal.getName(); // Email es el name de la autenticación
		User user = usersService.getUserByEmail(Email);
		Page<Offer> offers = offersService.getOffersForUser(pageable, user);
		model.addAttribute("offerList", offers.getContent());
		return "offer/list :: tableOffers";
	}

	@RequestMapping(value = "/offer/add", method = RequestMethod.POST)
	public String setOffer(@ModelAttribute Offer offer, Principal principal, BindingResult result) {
		offer.setUser(usersService.getUserByEmail(principal.getName()));
		offerAddFormValidator.validate(offer, result);
		if (result.hasErrors()) {
			return "offer/add";
		}
		offersService.addOffer(offer);
		return "redirect:/offer/list";
	}

	@RequestMapping("/offer/details/{id}")
	public String getDetail(Model model, @PathVariable Long id) {
		System.out.println(id);
		model.addAttribute("offer", offersService.getOffer(id));
		return "offer/details";
	}

	@RequestMapping("/offer/delete/{id}")
	public String deleteOffer(@PathVariable Long id) {
		offersService.deleteOffer(id);		
		return "redirect:/offer/mylist";
	}

	@RequestMapping(value = "/offer/add")
	public String getOffer(Model model) {
		model.addAttribute("usersList", usersService.getUsers());
		model.addAttribute("offer", new Offer());
		return "offer/add";
	}

	@RequestMapping(value = "/offer/edit/{id}")
	public String getEdit(Model model, @PathVariable Long id, Principal principal) {
		User sesion = usersService.getUserByEmail(principal.getName());
		Offer original = offersService.getOffer(id);
		if(original.getUser().equals(sesion)) {
		model.addAttribute("offer", original);
		model.addAttribute("usersList", usersService.getUsers());
		return "offer/edit";
		}
		else {
			return "offer/details/" + id;
		}
	}

	@RequestMapping(value = "/offer/edit/{id}", method = RequestMethod.POST)
	public String setEdit(Model model, @PathVariable Long id, @ModelAttribute Offer offer, BindingResult result) {
		Offer original = offersService.getOffer(id);
		offerEditFormValidator.validate(offer, result);
		if (result.hasErrors()) {
			return "offer/edit";
		}
		original.setPrice(offer.getPrice());
		original.setDescription(offer.getDescription());
		offersService.addOffer(original);
		return "redirect:/offer/details/" + id;
		
	}

	@RequestMapping(value = "/offer/{id}/resend", method = RequestMethod.GET)
	public String setResendTrue(Model model, @PathVariable Long id) {
		offersService.setOfferResend(true, id);
		return "redirect:/offer/list";
	}

	@RequestMapping(value = "/offer/{id}/noresend", method = RequestMethod.GET)
	public String setResendFalse(Model model, @PathVariable Long id) {
		offersService.setOfferResend(false, id);
		return "redirect:/offer/list";
	}
	
	@RequestMapping(value = "/offer/special/{id}", method = RequestMethod.GET)
	public String setSpecialTrue(Model model, @PathVariable Long id) {
		boolean result = offersService.setOfferSpecial(true,id); 
		if(result) 
			return "redirect:/offer/list";
		return "redirect:/cart/error";
		
	}

	@RequestMapping(value = "/offer/{id}/nospecial", method = RequestMethod.GET)
	public String setSpecialFalse(Model model, @PathVariable Long id) {
		offersService.setOfferSpecial(false,id); 
		return "redirect:/offer/list";
	}
	
	@RequestMapping("/offer/list")
	public String getList(Model model, Pageable pageable, Principal principal,
			@RequestParam(value = "", required=false) String searchText){
		
	String Email = principal.getName(); 
	User user = usersService.getUserByEmail(Email);
	Page<Offer> offers = new PageImpl<Offer>(new LinkedList<Offer>());
	if(searchText != null && !searchText.isEmpty()) {
		offers = offersService.searchOffersByTitle(pageable, searchText, user);
	}
	else {
	offers = offersService.getOffersByOthers(pageable, user);
	}
	model.addAttribute("offerList", offers.getContent()); 
	model.addAttribute("page", offers);
	return "offer/list";
	
	}
	
	@RequestMapping("/offer/mylist")
	public String getMyList(Model model, Pageable pageable, Principal principal,
			@RequestParam(value = "", required=false) String searchText){
		
	String Email = principal.getName(); 
	User user = usersService.getUserByEmail(Email);
	Page<Offer> offers = new PageImpl<Offer>(new LinkedList<Offer>());
	
	offers = offersService.getOffersForUser(pageable,user);
	model.addAttribute("offerList", offers.getContent()); 
	model.addAttribute("page", offers);
	return "offer/mylist";
	}
	
	@RequestMapping("/offer/speciallist")
	public String getSpecialList(Model model, Pageable pageable, Principal principal,
			@RequestParam(value = "", required=false) String searchText){
		
	String Email = principal.getName(); 
	User user = usersService.getUserByEmail(Email);
	Page<Offer> offers = new PageImpl<Offer>(new LinkedList<Offer>());

	offers = offersService.getSpecialOffersByOthers(pageable, user);
	
	model.addAttribute("offerList", offers.getContent()); 
	model.addAttribute("page", offers);
	return "offer/list";
	
	}
}
