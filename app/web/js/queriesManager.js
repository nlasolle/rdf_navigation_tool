const API_PATH = "http://localhost:8080/rdf-navigation-tool/";

var results;
var conditionCounts = [];

/**
 * Initialize the process by retrieving conditions from resource properties
 * @param {*} resource the resource iri
 */
function initProcess(resource, lang) {
    "use strict";

    var request = new XMLHttpRequest();

    //Retrieve all prefixes for the current RDF database (prefix + URI)
    request.open("POST", API_PATH + "init-process", true);
    request.setRequestHeader("Content-type", "application/json");

    request.onload = function () {
        if (request.status == 200) {
            conditions = JSON.parse(this.response);
            
            updateCheckboxes(conditions);
            executeCurrentStateQueries();
            
            $("#timeline").html("");
            $("#resultsCount").html(" 0 ");
            initSQTRLProcess();
        } else {
            console.log('An error occured when extracting attributes from the ressource ' + resource);
        }
    };

    let body = {
        "iri": resource,
        "lang": lang
    }

    
    request.send(JSON.stringify(body));
}

/**
 * Initialize the SPARQL query transformation process (in order to generate new conditions)
 */
function initSQTRLProcess() {
    "use strict";

    var request = new XMLHttpRequest();

    //Retrieve all prefixes for the current RDF database (prefix + URI)
    request.open("GET", API_PATH + "init-sqtrl-process", true);
    request.setRequestHeader("Content-type", "text/plain");

    request.onload = function () {
        if (request.status == 200) {
            //console.log("SPARQL query transformation process initialized.");
        } else {
            console.log('An error occured when initializing the SPARQL query transformation process.');
        }
    };
    request.send();
}

/**
 * Update initial resource details (iri link and label)
 */
function getResourceDetails(iri, lang) {
    var request = new XMLHttpRequest();
 
     //Retrieve all prefixes for the current RDF database (prefix + iri)
     request.open("POST", API_PATH + "resource-details?lang=" + lang, true);
 
     request.onload = function () {
 
         if (request.status == 200) {
             let details = JSON.parse(this.response);
             initializeResourceDetails(iri, details);
 
         } else {
             console.log('An error occured when extracting details for resource with iri ' + iri);
         }
     };
     request.send(iri);
}

/**
 * Retrieve an application configuration property
 */
function getProperty(property) {
    var request = new XMLHttpRequest();
 
     //Update the language service
     request.open("GET", API_PATH + 'configuration-service/' + property, true);
 
     request.onload = function () {
 
         if (request.status == 200) {
             var propertyValue = this.response;
         } else {
             console.log('An error occured when retrieving the value of property ${property}');
         }
     };
     
     request.send();
}

/**
 * Retrieve the use case initial resource
 */
function getInitialResource() {
    var request = new XMLHttpRequest();
 
     //Update the language service
     request.open("GET", API_PATH + 'configuration-service/initial', true);
 
     request.onload = function () {
 
         if (request.status == 200) {
             var initialResource = this.response;
             processInitialization(initialResource);
         } else {
             console.log('An error occured when retrieving the value of the initial resource');
         }
     };
     
     request.send();
}



/**
 * Retrieve an application configuration property
 */
function getItemLink() {
    var request = new XMLHttpRequest();
 
     //Update the language service
     request.open("GET", API_PATH + 'configuration-service/itemLink', true);
 
     request.onload = function () {
 
         if (request.status == 200) {
             itemLink = this.response;
         } else {
             console.log('An error occured when retrieving the value of property item link');
         }
     };
     
     request.send();
}

/**
 * Update an application configuration property
 */
function setProperty(property, value) {
    var request = new XMLHttpRequest();
 
     //Update the language service
     request.open("PUT", API_PATH + 'configuration-service/' +property, true);
 
     request.onload = function () {
 
         if (request.status == 200) {
             var response = this.response;
 
         } else {
             console.log('An error occured when updating the ${property} property.');
         }
     };

     request.send(value);
}

/**
 * Retrieve the label associated with date filtering property
 */
function getDatePropertyLabel(lang) {
    var request = new XMLHttpRequest();
 
     request.open("GET", API_PATH + "configuration-service/date-property-label?lang=" + lang, true);
 
     request.onload = function () {
 
         if (request.status == 200) {
            $("#dateLabel").html(this.response);
         } else {
             console.log("An error occured when retrieving the label of the date property!");
         }
     };
     
     request.send();
}

/**
 * Retrieve the labels associated with properties for which we print values for each result
 */
function getPropertiesLabels(lang) {
    var request = new XMLHttpRequest();
 

     request.open("GET", API_PATH + "configuration-service/properties-labels?lang=" + lang, true);
 
     request.onload = function () {
 
         if (request.status == 200) {
            propertiesLabels = JSON.parse(this.response);
         } else {
            console.log("An error occured when retrieving the labels of displayed properties");
         }
     };
     
     request.send();
}

/**
 * Retrieve the labels associated with all database resources
 */
function getResourcesLabels(type, lang) {
    var request = new XMLHttpRequest();
 
     //Update the language service
     request.open("POST", API_PATH + "configuration-service/resources-labels?lang=" + lang, true);
 
     request.onload = function () {
 
         if (request.status == 200) {
            var objects = JSON.parse(this.response);
            prepareObjectAutocompleteValues(objects);
            updateAdvancedSearchTable(objects);

         } else {
             console.log("An error occured when retrieving the labels of resources.");
         }
     };
     
     request.send(type);
}

/**
 * Retrieve the labels associated with all ontology properties
 */
function getOntologyProperties(lang) {
    var request = new XMLHttpRequest();
 
     //Update the language service
     request.open("GET", API_PATH + "configuration-service/ontology-properties?lang=" + lang, true);
 
     request.onload = function () {
 
         if (request.status == 200) {
            var properties = JSON.parse(this.response);
            preparePropertyAutocompleteValues(properties);
         } else {
             console.log("An error occured when retrieving the labels of ontology properties.");
         }
     };
     
     request.send();
}

/**
 * Retrieve the labels associated with all database classes
 */
 function getTypes(lang) {
    var request = new XMLHttpRequest();
 
     //Update the language service
     request.open("GET", API_PATH + "configuration-service/types?lang=" + lang, true);
 
     request.onload = function () {
 
         if (request.status == 200) {
            var types = JSON.parse(this.response);
           
            prepareTypesSelection(types);
         } else {
             console.log("An error occured when retrieving the labels of database classes.");
         }
     };
     
     request.send();
}

/**
 * Retrieve the min and max dates slider options
 */
function getDateFilteringOptions() {
    var request = new XMLHttpRequest();
 
     //Update the language service
     request.open("GET", API_PATH + 'configuration-service/date-options', true);
 
     request.onload = function () {
 
         if (request.status == 200) {
             options = JSON.parse(this.response);
             initializeDateSlider();
             prepareModalChart(options.min, options.max);
            //$("#dateLabel").html(this.response);
         } else {
             console.log("An error occured when retrieving the date filtering options.");
         }
     };
     
     request.send();
}

function executeCurrentStateQueries() {
    "use strict";
    var request = new XMLHttpRequest();

    //Retrieve all prefixes for the current RDF database (prefix + URI)
    request.open("GET", API_PATH + "execute-queries", true);

    request.onload = function () {

        if (request.status == 200) {
            var conditionsIds = [];

            $('.list-group-item').each(function () {
                conditionsIds.push($(this).val());
            });

            getConditionsMatches(conditionsIds);

        } else {
            console.log("An error occured when executing queries to find results");
        }
    };
    request.send();
}

function getQueryResults(conditionsIds, negativeConditionsIds, mode, lang) {
    "use strict";
    var request = new XMLHttpRequest();

    //Retrieve all prefixes for the current RDF database (prefix + URI)
    request.open("POST", API_PATH + "get-results?mode=" + mode, true);
    request.setRequestHeader("Content-type", "application/json");

    request.onload = function () {
        // Begin accessing JSON data here
        if (request.status == 200) {
            results = JSON.parse(this.response);
            var slider = document.getElementById("date-slider");
            updateBoxes(results, slider.noUiSlider.get()[0], slider.noUiSlider.get()[1]);
            updateChart(results);
        } else {
            console.log('An error occured when retrieving result for checked conditions!');
        }
    };

    var param = {};
    param.conditionsIds = conditionsIds;
    param.negativeConditionsIds = negativeConditionsIds;
    param.lang = lang;
    request.send(JSON.stringify(param));
}

function getConditionsMatches(conditionsIds) {
    "use strict";
    var request = new XMLHttpRequest();

    //Retrieve all prefixes for the current RDF database (prefix + URI)
    request.open("POST", API_PATH + "get-results?mode=2", true);
    request.setRequestHeader("Content-type", "application/json");

    request.onload = function () {
        // Begin accessing JSON data here
        if (request.status == 200) {
            results = JSON.parse(this.response);

            //Init all counts to 0 because we use increment then
            conditionCounts = [];
            
            for (i in conditionsIds) {
                conditionCounts[i] = 0;
            }


            //NOTE : for this section, there is an issue related to the Or mode when aggregating result
            //The Java aggregator does not merge results

            for (i in results) {
                for (var j in results[i].conditions) {
                    conditionCounts[results[i].conditions[j]]++;
                }

            }

            for (i in conditionCounts) {
                if (!$("#condition" + i).html().includes("badge")) {
                    $("#condition" + i).append('<span class="badge">' + conditionCounts[i] + '</span>');
                }
            }

        } else {
            console.log('An error occured when retrieving result for all conditions!');
        }
    };

    var param = {};
    param.conditionsIds = conditionsIds;
    param.negativeConditionsIds = [];
    request.send(JSON.stringify(param));
}

/**
 * Generates a new condition thanks to the application of a SPARQL query transformation rule
 * @param {*} lang
 */
function generateNewCondition(lang) {
    "use strict";
    var request = new XMLHttpRequest();

    //Retrieve all prefixes for the current RDF database (prefix + URI)
    request.open("GET", API_PATH + "get-more?lang=" + lang, true);
    request.setRequestHeader("Content-type", "application/json");

    request.onload = function () {
        // Begin accessing JSON data here
        if (request.status == 200) {
            let expressions = JSON.parse(this.response);

            if (expressions.length != 0) {
                for(var i in expressions){
                    addNewCheckbox(conditions.length, expressions[i]);
                    conditions.push(expressions[i]);
                }

                executeCurrentStateQueries();
            } else {
                alert("Aucune nouvelle condition générée !");
                $("#moreButton").prop("disabled", true);
            }
        } else {
            console.log("An error occured when adding a new condition.");
        }
    };


    request.send();
}

/**
 * Retrieve the expressions for the given language
 */
function getTranslatedExpressions(lang) {
    var request = new XMLHttpRequest();
 

     request.open("GET", API_PATH + "get-translated-expressions?lang=" + lang, true);
 
     request.onload = function () {
 
         if (request.status == 200) {
            let translatedExpressions = JSON.parse(this.response);
            updateCheckboxes(translatedExpressions);
            executeCurrentStateQueries();
         } else {
            console.log("An error occured when retrieving the translated expressions.");
         }
     };
     
     request.send();
}

/**
 * 
 * @param {*} lang the language tag
 */
function addNewCondition(lang) {
    "use strict";
   
    let propertyInputValue = $("#propertyInput").val();
    let objectInputValue = $("#objectInput").val();


    //Add the new condition to the backend application list of expressions
    var request = new XMLHttpRequest();
    
    request.open("POST", API_PATH + "add-condition", true);
    request.setRequestHeader("Content-type", "application/json");

    request.onload = function () {

        if (request.status == 200) {
            //The API returns false if the conditions already exists in the list (from a previous generation or manual addition)
            if(!JSON.parse(this.response)){
                alert("The condition is already existing!")
            } else {
                //Add the new condition to the interface
                let expression = propertyInputValue + " " + objectInputValue;
                addNewCheckbox(conditions.length, expression);
                conditions.push(expression);

                executeCurrentStateQueries();
            }
        } else {
            console.log("An error occured when adding a new manual condition.");
        }
    };
    let body = {
        "property": inputProperties[propertyInputValue],
        "propertyLabel": propertyInputValue,
        "resource": inputObjects[objectInputValue],
        "resourceLabel": objectInputValue,
        "lang": lang
    }

    
    request.send(JSON.stringify(body));
}