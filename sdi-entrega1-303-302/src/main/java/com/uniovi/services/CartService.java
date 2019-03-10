package com.uniovi.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uniovi.entities.Offer;
import com.uniovi.repositories.OffersRepository;
@Service
@Transactional
public class CartService {
	
	private OffersRepository offersRepository;

	private List<Offer> articles = new ArrayList<>();
	
	@Autowired
	public CartService(OffersRepository offersRepository) {
		this.offersRepository = offersRepository;
	}

	public void addOffer(Long id) {
		Offer offer = offersRepository.findById(id).get();
		if (offer != null) {
			articles.add(offer);
		}
	}

	public void removeOffer(Long id) {
		Optional<Offer> findById = offersRepository.findById(id);
		if (articles.contains(findById)) {
			articles.remove(articles.indexOf(findById));
		}
	}

	public List<Offer> getOffers() {
		return new ArrayList<Offer>(articles);
	}

	public void checkout() {
		for (Offer offer : articles) {
			offersRepository.deleteById(offer.getId());
		}
		articles.clear();
	}

	public double getPrice() {
		double total = 0;
		for (Offer offer : articles) {
			total += offer.getPrice();
		}
		return total;
	}
}