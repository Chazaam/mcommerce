package com.clientui.controller;


import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.clientui.beans.CommandeBean;
import com.clientui.beans.ProductBean;
import com.clientui.proxies.MicroserviceCommandeProxy;
import com.clientui.proxies.MicroserviceProduitsProxy;

@Controller
public class ClientController {
	
	@Autowired
	private MicroserviceProduitsProxy ProduitsProxy;
	
	@Autowired
	private MicroserviceCommandeProxy CommandesProxy;
	
	/* Etape 1 Liste des produits */
	@RequestMapping("/")
	public String accueil(Model model) {
		List<ProductBean> produits = ProduitsProxy.listeDesProduits();
		model.addAttribute("produits", produits);
		
		return "Accueil";
	}
	
	/* Etape 2 Détail d'un produit */
	@RequestMapping("/details-produit/{id}")
	public String ficheProduit(@PathVariable int id,  Model model){
		ProductBean produit = ProduitsProxy.recupererUnProduit(id);
		model.addAttribute("produit", produit);
		return"FicheProduit";
	}
	
	/* Etape 3 et 4 Commande d'un produit */
	@RequestMapping("/details-produit/commander-produit/{id}")
	public String passerCommande(@PathVariable int id, Model model) {
		
		// Création de la commande
		CommandeBean commande = new CommandeBean();
		commande.setProductId(id);
		commande.setQuantite(1);
		commande.setDateCommande(new Date());
		
		// Création de la commande auprès du microservice de commande
		CommandeBean commandeAjoutee = CommandesProxy.ajouterCommande(commande);
		
		// La commande ne contient qu'un seul produit dont on récupère le prix sinon il faudrait récupérer l'ensemble des prix
		ProductBean produit = ProduitsProxy.recupererUnProduit(id);
		double montant = produit.getPrix();
		
		// Envoie des données à la vue de Paiement de la commande
		model.addAttribute("commande", commandeAjoutee);
		model.addAttribute("montant", montant);
		
		return "Paiement";
	}

	
}
