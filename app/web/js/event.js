$(document).ready(function(){
  $('#searchTable').DataTable( {
    pagingType: "simple", // "simple" option for 'Previous' and 'Next' buttons only
    bFilter: true,
    autoWidth: false
  } );

  $('#searchTable tbody').on( 'click', 'tr', function () {
    $('#searchTable').DataTable().$('tr.selected').removeClass('selected');
    $(this).toggleClass('selected');

    if($(this).hasClass('selected')){
      $('#resourceModification').removeAttr('disabled');
      let iri = $('#searchTable').DataTable().row('.selected').data()[1];

      $('#resourceInput').prop('value', iri);
    } else {
      $('#resourceModification').attr('disabled', 'true');
      $('#resourceInput').prop('value', '');
    }

  } );


    //English for the default page language
    setLanguage(0);

    getDateFilteringOptions();

    //Init an application process for the given resource iri
    applicationInitialization();
    //iri = "http://henripoincare.fr/api/items/5834"; //Letter of the Henri Poincaré corpus
    //iri = "http://dbpedia.org/resource/Guernica_(Picasso)"; //Guernica
    //iri = "http://dbpedia.org/resource/Pride_and_Prejudice"; //Orgueils et préjugés
    //iri = "http://data.bnf.fr/ark:/12148/cb119675961#about"; //Notre Dame de Paris
    //iri = "http://isidore.science/document/10670/1.wg6tui"; //Isidore
    //iri = "http://urn.fi/URN:NBN:fi:bib:me:W00599099000"; //Finland library
    //iri = "http://dbpedia.org/resource/Man-Child"; //DBpedia Music album
    //iri = "http://dbpedia.org/resource/Alice's_Adventures_in_Wonderland"; //Literary works
    //iri = "http://dbpedia.org/resource/The_Little_Prince";
    //iri = "http://dbpedia.org/resource/Froth_on_the_Daydream";
    //iri = "http://dbpedia.org/resource/Dune_(novel)";
    //iri = "http://dbpedia.org/resource/Strange_Case_of_Dr_Jekyll_and_Mr_Hyde";
    //iri = "http://dbpedia.org/resource/The_Hound_of_the_Baskervilles";
    
    getInitialResource();
    
    $('[data-toggle="tooltip"]').tooltip(); 

    //All buttons, input listeners  
    $('#btn').click(function() {
        $('#wizard').toggle();
    });


    $('#languageSelect').change(function() {
        let value = $("option:selected", this).val();
        lang = langData.tag[value];

        setLanguage(value);
        getResourceDetails(iri, lang);
        getPropertiesLabels(lang);
        getDatePropertyLabel(lang); 
        getOntologyProperties(lang);
        getResourcesLabels(initialResourceType, lang);
        getTypes(lang);
        getTranslatedExpressions(lang);
        let slider = document.getElementById('date-slider');
        setTimeout(() => {  updateBoxes(results, slider.noUiSlider.get()[0], slider.noUiSlider.get()[1]); }, 250); 

  });

    $('#validation').click(function(e) {
        var conditionsIds = [], negativeConditionsIds = [];
        let first = true;

        searchDescription = "";

        let mode = $('input[name=modeChoice]:checked').val()

        if(mode == 1){
          $('.list-group-item-success').each(function () {    
            conditionsIds.push($(this).val());

            if(first) {
              searchDescription = conditions[$(this).val()];
              first = false;
            } else {
              searchDescription += " et " + conditions[$(this).val()];
            }
          });

          $('.list-group-item-danger').each(function () {    
            negativeConditionsIds.push($(this).val());

            if(first) {
              searchDescription = "Non " + conditions[$(this).val()];
              first = false;
            } else {
              searchDescription += " et non " + conditions[$(this).val()];
            }
          });
        } else {
          $('.list-group-item-success').each(function () {    

            conditionsIds.push($(this).val());

            if(first) {
              searchDescription = conditions[$(this).val()];
              first = false;
            } else {
              searchDescription += " ou " + conditions[$(this).val()];
            }
          });
        }

        getQueryResults(conditionsIds, negativeConditionsIds, mode, lang);
        

    });

    $('#resourceModification').attr('disabled', true);   
    $('#conditionAddValidation').attr('disabled', true);   
    $('#moreButton').attr('disabled', true);  //Only for public export due to application failing to apply rules for the DBpedia endpoint

    //Manual condition add validation button should be visible if property and value fields have been completed
    $('#propertyInput').keyup(function() {

      if($('#propertyInput').val() != "" && $('#objectInput').val() != "") {
         $('#conditionAddValidation').removeAttr('disabled');
      } else {
         $('#conditionAddValidation').attr('disabled', true);   
      }
    });

    $('#objectInput').keyup(function() {
      if($('#propertyInput').val() != "" && $('#objectInput').val() != "") {
         $('#conditionAddValidation').removeAttr('disabled');
      } else {
         $('#conditionAddValidation').attr('disabled', true);   
      }
    });

    $('#resourceTypeSelect').change(function(e) {
      getResourcesLabels($(this).val(), lang);
    });

    $('#resourceInput').change(function(e) {
      if($(this).val() == ''){
        $('#resourceModification').attr('disabled', true);
        
      } else {
        $('#resourceModification').removeAttr('disabled');
      }
    });

    $('#typesSelect').change(function(e) {
      getResourcesLabels($(this).val(), lang);
    });
    
    $('#csvButton').click(function(e) {
      downloadCSV({ filename: "query-results.csv", data: results});
    });

    $('#chartCSVButton').click(function(e) {
      downloadChartCSV(years, counts);
    });

    $('#resourceModification').click(function(e) {
      processInitialization($('#resourceInput').val());
    });
    
    $('#moreButton').click(function(e) {
      generateNewCondition(lang);
    });

    $('#conditionAddValidation').click(function(e) {
      addNewCondition(lang);
    });

    $('#chartModal').on('shown.bs.modal', function () {
      $('#chartModal').trigger('focus');
    })

    $('#changeButton').on('click', function () {
      if(initialResourceType){
        getResourcesLabels(initialResourceType, lang);
      } else {
        getResourcesLabels("http://www.w3.org/2002/07/owl#", lang);
      }
    })

    $("#viewSwitch").change(function() {
      if($(this).is(":checked")) {
        for(var year = options.min; year <= options.max; year++){
          $('#' + year + "-toggle").hide("3000");
          $('#' + year + "-div").prop("textContent", year + " | " + resultsYearCounts[year] + " results");
        } 
      } else {
        for(var year = options.min; year <= options.max; year++){
          $('#' + year + "-toggle").show("3000");
          $('#' + year + "-div").prop("textContent", year);
        } 
         
      }
  });

    /*
   $('#moreModal').on('shown.bs.modal', function () {
      $('#moreModal').trigger('focus')
    })*/

});

/**
 * Launch a new application process centered around the given resource (refresh the user interface)
 * @param {*} iri the resource identifier
 */
function processInitialization(iri){
      //Launch a process centered around this resource
      initProcess(iri, lang);

      //Retrieve nitial resource details
      getResourceDetails(iri, lang);
}

/**
 * Initialize the user interface for the first application launch
 */
function applicationInitialization(){
  
  //Options and labels retrieval for different interface features
  getItemLink();
  getDatePropertyLabel(lang);
  getPropertiesLabels(lang);
  getOntologyProperties(lang);
  getTypes(lang);
}