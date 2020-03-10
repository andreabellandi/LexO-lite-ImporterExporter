/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.lexoimporter.lexiconUtil;

import it.cnr.ilc.lc.lexoimporter.CoNLLImporter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 *
 * @author andrea
 */
public class LexiconUtils {
 
    
    
    public static void createLexicon(String lang) {
    }
    
    public static void createWord(String lang, String form, String lemma, String finGrainPoS, String coarseGrainPoS,
            String firstTraitGroup, String secondTraitGroup, String thirdTraitGroup, OWLOntologyManager manager) {
        
        createLemma(Constant.WORD_TYPE, lang, form, lemma, finGrainPoS, coarseGrainPoS, firstTraitGroup, secondTraitGroup, thirdTraitGroup, manager);
        if (!form.equals(lemma)) {
            createForm(Constant.WORD_TYPE, lang, form, lemma, finGrainPoS, coarseGrainPoS, firstTraitGroup, secondTraitGroup, thirdTraitGroup, manager);
        }
    }
    
    public static void createLemma(String type, String lang, String form, String lemma, String finGrainPoS, String coarseGrainPoS,
            String firstTraitGroup, String secondTraitGroup, String thirdTraitGroup, OWLOntologyManager manager) {
            
            String lemmaInstance = getIRI(lemma, lang, "lemma");
            String senseInstance = getIRI(lemma, lang, "sense1");
            String entryInstance = getIRI(lemma, lang, "entry");
        
            OWLNamedIndividual lexicon = getIndividual(Constant.LEXICON_INDIVIDUAL_NAME, Namespace.LEXICON, manager);
            OWLNamedIndividual le = getEntry(entryInstance, OntoLexEntity.Class.WORD.getLabel(), manager);
            OWLNamedIndividual cf = getForm(lemmaInstance, manager);
            OWLNamedIndividual s = getSense(senseInstance, manager);
        
            addObjectPropertyAxiom(OntoLexEntity.ObjectProperty.ENTRY.getLabel(), lexicon, le, Namespace.LIME, manager);
            addObjectPropertyAxiom(OntoLexEntity.ObjectProperty.CANONICALFORM.getLabel(), le, cf, Namespace.ONTOLEX, manager);
            addObjectPropertyAxiom(OntoLexEntity.ObjectProperty.SENSE.getLabel(), le, s, Namespace.ONTOLEX, manager);
            addDataPropertyAxiom(OntoLexEntity.DataProperty.WRITTENREP.getLabel(), cf, lemma, Namespace.ONTOLEX, manager);
            
            setMoprhology(type, le, cf, finGrainPoS, coarseGrainPoS, firstTraitGroup, secondTraitGroup, thirdTraitGroup, manager);
    }

    public static void createForm(String type, String lang, String form, String lemma, String finGrainPoS, String coarseGrainPoS,
            String firstTraitGroup, String secondTraitGroup, String thirdTraitGroup, OWLOntologyManager manager) {
               
            String formInstance = getIRI(lemma, lang, form, "form");
            String entryInstance = getIRI(lemma, lang, "entry");
            OWLNamedIndividual le = getIndividual(entryInstance, OntoLexEntity.Class.WORD.getLabel(), manager);
            OWLNamedIndividual of = getForm(formInstance, manager);

            addObjectPropertyAxiom(OntoLexEntity.ObjectProperty.OTHERFORM.getLabel(), le, of, Namespace.ONTOLEX, manager);

            setMoprhology(type, le, of, finGrainPoS, coarseGrainPoS, firstTraitGroup, secondTraitGroup, thirdTraitGroup, manager);
    }
    
    private static void setMoprhology(String type, OWLNamedIndividual le, OWLNamedIndividual cf, String finGrainPoS, String coarseGrainPoS,
            String firstTraitGroup, String secondTraitGroup, String thirdTraitGroup, OWLOntologyManager manager) {
            
            addObjectPropertyAxiom("partOfSpeech", cf, 
                    (Constant.WORD_TYPE.equals(type) ? getIndividual(CoNLLMapToLexInfo.posMapping.get(finGrainPoS), Namespace.LEXINFO, manager) : 
                            getIndividual(CoNLLMapToLexInfo.phraseTypeMapping.get(finGrainPoS), Namespace.LEXINFO, manager)),
                    Namespace.LEXINFO, manager);
            addObjectPropertyAxiom("gender", cf, 
                            getIndividual(CoNLLMapToLexInfo.morphoTraitMapping.get(firstTraitGroup.split("\\|")[0].split("=")[1]), Namespace.LEXINFO, manager),
                    Namespace.LEXINFO, manager);
            addObjectPropertyAxiom("number", cf, 
                            getIndividual(CoNLLMapToLexInfo.morphoTraitMapping.get(firstTraitGroup.split("\\|")[1].split("=")[1]), Namespace.LEXINFO, manager),
                    Namespace.LEXINFO, manager);
            
            addDataPropertyAxiom("valid", le, "false", Namespace.DCT, manager);
    }
    
    public static void createMultiWord(String lang, String form, String lemma, String finGrainPoS, String coarseGrainPoS,
            String firstTraitGroup, String secondTraitGroup, String thirdTraitGroup, OWLOntologyManager manager) {
        
        createLemma(Constant.MULTIWORD_TYPE, lang, form, lemma, finGrainPoS, coarseGrainPoS, firstTraitGroup, secondTraitGroup, thirdTraitGroup, manager);
        if (!form.equals(lemma)) {
            createForm(Constant.MULTIWORD_TYPE, lang, form, lemma, finGrainPoS, coarseGrainPoS, firstTraitGroup, secondTraitGroup, thirdTraitGroup, manager);
        }
        // TODO: decomposition
    }   

    public static String getIRI(String... params) {
        StringBuilder iri = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            iri.append(params[i]);
            if (i < (params.length - 1)) {
                iri.append("_");
            }
        }
        return iri.toString();
    }
    
    private static OWLNamedIndividual getEntry(String uri, String clazz, OWLOntologyManager manager) {
        OWLClass lexicalEntryClass = manager.getOWLDataFactory().getOWLClass(Namespace.ONTOLEX, clazz);
        OWLNamedIndividual lexicalEntry = manager.getOWLDataFactory().getOWLNamedIndividual(Namespace.LEXICON, uri);
        addIndividualAxiom(lexicalEntryClass, lexicalEntry, manager);
        return lexicalEntry;
    }

    private static OWLNamedIndividual getForm(String uri, OWLOntologyManager manager) {
        OWLClass lexicalFormClass = manager.getOWLDataFactory().getOWLClass(Namespace.ONTOLEX, OntoLexEntity.Class.FORM.getLabel());
        OWLNamedIndividual form = manager.getOWLDataFactory().getOWLNamedIndividual(Namespace.LEXICON, uri);
        addIndividualAxiom(lexicalFormClass, form, manager);
        return form;
    }
    
    private static OWLNamedIndividual getSense(String uri, OWLOntologyManager manager) {
        OWLClass lexicalSenseClass = manager.getOWLDataFactory().getOWLClass(Namespace.ONTOLEX, OntoLexEntity.Class.LEXICALSENSE.getLabel());
        OWLNamedIndividual sense = manager.getOWLDataFactory().getOWLNamedIndividual(Namespace.LEXICON, uri);
        addIndividualAxiom(lexicalSenseClass, sense, manager);
        return sense;
    }

    private static OWLNamedIndividual getSense(String senseName, int n, OWLOntologyManager manager) {
        OWLClass lexicalSenseClass = manager.getOWLDataFactory().getOWLClass(Namespace.ONTOLEX, "LexicalSense");
        OWLNamedIndividual sense = manager.getOWLDataFactory().getOWLNamedIndividual(Namespace.LEXICON, senseName + n);
        addIndividualAxiom(lexicalSenseClass, sense, manager);
        return sense;
    }

    private static OWLNamedIndividual getIndividual(String uri, String ns, OWLOntologyManager manager) {
        return manager.getOWLDataFactory().getOWLNamedIndividual(ns, uri);
    }
   
    private static void addIndividualAxiom(OWLClass c, OWLNamedIndividual i, OWLOntologyManager manager) {
        OWLClassAssertionAxiom classAssertion = manager.getOWLDataFactory().getOWLClassAssertionAxiom(c, i);
        manager.addAxiom(manager.getOntology(IRI.create(Namespace.LEXICON.replace("#", ""))), classAssertion);
    }
    
    private static void addObjectPropertyAxiom(String objProp, OWLNamedIndividual src, OWLNamedIndividual trg, String objPropNs, OWLOntologyManager manager) {
        OWLObjectProperty p = manager.getOWLDataFactory().getOWLObjectProperty(objPropNs, objProp);
        OWLObjectPropertyAssertionAxiom propertyAssertion = manager.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(p, src, trg);
        manager.addAxiom(manager.getOntology(IRI.create(Namespace.LEXICON.replace("#", ""))), propertyAssertion);
    }

    private static void addDataPropertyAxiom(String dataProp, OWLNamedIndividual src, String trg, String ns, OWLOntologyManager manager) {
        if (!trg.isEmpty()) {
            OWLDataProperty p = manager.getOWLDataFactory().getOWLDataProperty(ns, dataProp);
            OWLDataPropertyAssertionAxiom dataPropertyAssertion = manager.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(p, src, trg);
            manager.addAxiom(manager.getOntology(IRI.create(Namespace.LEXICON.replace("#", ""))), dataPropertyAssertion);
        }
    }
        // params: langName, uriLang, lingCat, descritpion, creator
//    public void addNewLangLexicon(String... params) {
//        OWLClass lexiconClass = factory.getOWLClass(pm.getPrefixName2PrefixMap().get("lime:"), "Lexicon");
//        OWLNamedIndividual lexiconEntry = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), params[0] + "_lexicon");
//        addIndividualAxiom(lexiconClass, lexiconEntry);
//        addDataPropertyAxiom("language", lexiconEntry, params[0], pm.getPrefixName2PrefixMap().get("lime:"));
//        addDataPropertyAxiom("language", lexiconEntry, params[1], pm.getPrefixName2PrefixMap().get("dct:"));
//        addDataPropertyAxiom("linguisticCatalog", lexiconEntry, params[2], pm.getPrefixName2PrefixMap().get("lime:"));
//        addDataPropertyAxiom("description", lexiconEntry, params[3], pm.getPrefixName2PrefixMap().get("dct:"));
//        addDataPropertyAxiom("creator", lexiconEntry, params[4], pm.getPrefixName2PrefixMap().get("dct:"));
//    }
//        private static void setPrefixes() {
//        pm = new DefaultPrefixManager();
//        pm.setPrefix("lexicon", "");
//        pm.setPrefix("lexinfo", "https://www.lexinfo.net/ontology/2.0/lexinfo#");
//        pm.setPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
//        pm.setPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
//        pm.setPrefix("ontolex", "http://www.w3.org/ns/lemon/ontolex#");
//        pm.setPrefix("lime", "http://www.w3.org/ns/lemon/lime#");
//        pm.setPrefix("dct", "http://purl.org/dc/terms/");
//        pm.setPrefix("decomp", "http://www.w3.org/ns/lemon/decomp#");
//        pm.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
//        pm.setPrefix("vartrans", "http://www.w3.org/ns/lemon/vartrans#");
//        pm.setPrefix("trcat", "http://purl.org/net/translation-categories#");
//        pm.setPrefix("synsem", "http://www.w3.org/ns/lemon/synsem#");
//    }
    
}