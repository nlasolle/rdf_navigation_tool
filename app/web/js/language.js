var langData = {
    "tag": [
        "en",
        "fr"
    ],
    "title": [
      "RDF graph navigation tool",
      "Exploration de corpus du Web sémantique"
    ],
    "resourceBlock" : [
        "Initial resource",
        "Ressource initiale"
    ],
    "searchBlock" : [
        "Search parameters",
        "Paramètres de la recherche"
    ],
    "advancedSearch" : [
        "Advanced search",
        "Recherche avancée"
    ],
    "resourceTooltip" : [
        "No description has been retrieved for this resource.",
        "Aucun description n'a été retrouvée pour cette ressource."
    ],
    "optionSelection" : [
        "--- Select a type ---",
        "--- Veuillez sélectionner un type ---"
    ],
    "resultsBlock" : [
        "Results:",
        "Résultats :"
    ],
    "searchButton" : [
        "Query",
        "Rechercher"
    ],
    "moreButton" : [
        "Generate condition",
        "Générer une condition"
    ],
    "exportLabel" : [
        "Export results",
        "Exporter les résultats"
    ],
    "exportButton" : [
        "CSV",
        "CSV"
    ],
    "distributionLabel" : [
        "Visualize distribution",
        "Afficher la distribution"
    ],
    "distributionButton" : [
        "Show",
        "Afficher"
    ],
    "allConditionLabel" : [
        "All conditions",
        "Toutes les conditions"
    ],
    "oneConditionLabel" : [
        "One of",
        "Au moins une"
    ],
    "chartLabel" : [
        "Results distribution by year",
        "Nombre de résultats par année"
    ],
    "addCondition" : [
        "Add condition",
        "Ajouter une condition"
    ],
    "resourceType" : [
        "Type of the target resource",
        "Type de la ressource cible"
    ],
    "resourceValue" : [
        "Label",
        "Label"
    ],
    "newSearch" : [
        "New Search",
        "Nouvelle recherche"
    ],
    "details" : [
        "Details",
        "Détails"
    ],
    "resultsCountLabel" : [
        "resources",
        "ressources"
    ],
    "chartExportButton" : [
        "CSV export",
        "Exporter en CSV"
    ],
    "chartModalClose" : [
        "Close",
        "Fermer"
    ],
    "conditionsModalTitle" : [
        "Manual condition creation",
        "Ajout manuel de condition"
    ],
    "conditionModalClose" : [
        "Close",
        "Fermer"
    ],
    "conditionModalValidate" : [
        "Validate",
        "Valider"
    ],
    "resourceModalTitle" : [
        "Give the IRI of the resource",
        "Donner l'identifiant (IRI) de la ressource"
    ],
    "resourceModalClose" : [
        "Close",
        "Fermer"
    ],
    "resourceModalValidate" : [
        "Validate",
        "Valider"
    ],
    "propertyCondition" : [
        "Property",
        "Propriété"
    ],
    "valueCondition" : [
        "Value",
        "Valeur"
    ],
    "switchLabel" : [
        "Compact view",
        "Vue compacte"
    ],
    "changeLabel" : [
        "Update",
        "Modifier"
    ]
    
  };

/**
 * Set the language associated with the user interface
 * @param {*} id the param of the selected laguage
 */
function setLanguage(id){

    for([key, value] of Object.entries(langData)){
        $("#" + key).html(value[id]);

        $('.' + key).each(function() {
            $(this).html(value[id]);
         });
    }
}