var propertiesLabels;

var conditions = [];
var notConditions = [];
var resultsYearCounts; 
var options = {};
var iri;
var lang = "en";
var inputProperties, inputObjects;
var itemLink;
var initialResourceType, initialResourceTypeLabel;

var searchDescription; //Contains the string description of the last searched action

const MIN_DATE = 1720, MAX_DATE = 1780;

const frenchMonthNames = ["Mois inconnu", "janvier", "février", "mars", "avril", "mai", "juin",
"juillet", "août", "septembre", "octobre", "novembre", "décembre"];

const englishMonthNames = ["Unknown month", "January", "February", "March", "April", "May", "June",
"July", "August", "September", "October", "November", "December"];

function filterWithDates(beginning, end){
    for(var date = MIN_DATE; date < beginning; date++){
        if(document.getElementById(date + "section")){    
            document.getElementById(date + "section").style.visibility = "hidden";
        }
    }

    if(MAX_DATE > end){
        for(var date = end + 1; date < MAX_DATE; date++){
            if(document.getElementById(date + "section")){
                document.getElementById(date + "section").style.visibility = "hidden";
            }
        }
    }

    for(var date = beginning; date <= end; date++){
        if(document.getElementById(date + "section")){
            document.getElementById(date + "section").style.visibility = "visible";
        }
    }
}

function updateBoxes(results, beginning, end){
    var timeline = document.getElementById("timeline");
    timeline.innerHTML = "";
    var previousYear = "", currentYear = "", currentMonth = -1, previousMonth = -1;
    let yearResultsCount = 0, totalResultsCount = 0;
    var langId= $("option:selected", languageSelect).val();
    resultsYearCounts = {};

    for(i in results){
        currentYear = results[i].date.substring(0, 4);

        if(currentYear >= beginning && currentYear <= end){
            if (currentYear != previousYear){
                //We save the final count of results for the previous year
                resultsYearCounts[previousYear] = yearResultsCount;
                
                yearResultsCount = 0 ;
                addYear(currentYear, timeline);
                previousYear = currentYear;
                currentMonth = -1;
                previousMonth = -1;
            }
            
            currentMonth = results[i].date.length <= 4 ? 0 : parseInt(results[i].date.substring(5,7));
   
            if (currentMonth != previousMonth){  
                addMonth(currentYear, currentMonth);
                previousMonth = currentMonth;
            }

            addBox(currentYear + "-" + currentMonth + "-row", results[i], langId);
            yearResultsCount++;
            totalResultsCount++;
        }

    }
    resultsYearCounts[previousYear] = yearResultsCount; //Last year total count
    $("#resultsCount").html(" " + totalResultsCount + " ");
}

function addYear(year, parent, count){
    var content = document.createElement('div');
    content.id = year+"-div";

    content.addEventListener('click', function (event) {
        $('#' + year + "-toggle").toggle("3000");
    });

    content.className = "timeline-year";
    content.textContent = year; //+ " | " + count + " results";

    var section = document.createElement('div');
    section.id = year + "section";
    section.className = "timeline-section";

    parent.appendChild(content);
    parent.appendChild(section);
   
}

function addMonth(year, month){
    var content = document.createElement('div');
    content.id = year + "-" + month + "-div";
    content.className = "timeline-month";

    let langId= $("option:selected", languageSelect).val();

    if(langId == 1){
        content.textContent =  frenchMonthNames[month];
    } else {
        content.textContent =  englishMonthNames[month];
    }
    
    var monthRow = document.createElement('div');
    monthRow.className = "row";
    monthRow.id = year + "-" + month + "-row";
  
    var toggleDiv = document.getElementById(year + "-toggle");

    if(!toggleDiv) {
        toggleDiv = document.createElement('div');
        toggleDiv.id = year + "-toggle";
    } 

    toggleDiv.appendChild(content);
    toggleDiv.appendChild(monthRow);
    parent = document.getElementById(year + "section"); // The timeline section
    parent.appendChild(toggleDiv);
}

function addBox(parentId, result, langId){
    
    var uri = itemLink + result.uri.substring(result.uri.lastIndexOf("/") + 1);

    //Construct the box (The writing date as the title, )
    let htmlCode = "<div class='col-sm-4'>" +
            "<a target='_blank' href='" + uri + "'>" +
                        "<div class='timeline-box'>" +
                            "<div class='box-title'>" +
                            result.date +
                            "</div>" +
                            "<div class='box-content'>";

    var displayedValues = [];

    for(property in result.properties){

        let propertyLabel = propertiesLabels[property];
        let value = result.properties[property];
        if(propertyLabel) {
            displayedValues[propertyLabel] = value;
        } else {
            //In this situation, no label has been retrieved for the given property. We instead use the property IRI for which we capitilize the first letter
            var propertyName = property.charAt(0).toUpperCase() + property.slice(1); 
            displayedValues[propertyName] = value;
        }
    }

    for(property in displayedValues){
        htmlCode+="<div class='box-item'><strong>" + property + "</strong> " + displayedValues[property] + "</div>";
    }

    htmlCode+=
       "</div>" +
        "<div class='box-footer'>";

        htmlCode += "<a target='_blank' href='" + uri + "'class='btn btn-more btn-sm' style='text-align: right'>" + langData.details[langId] + "</a>" +
        "<button target='_blank' onclick='processInitialization(\"" + result.uri + "\");' class='btn btn-more btn-sm newSearch' style='text-align:right; ; margin-left: 10px;'>"
         +  langData.newSearch[langId]  + " </button></div>" +
            "</div>" +
        "</div></a>";
        
    

    //Add the value of the generated box to the parent div
    if(document.getElementById(parentId)) document.getElementById(parentId).innerHTML+= htmlCode;
}

function updateCheckboxes(conditions){

    $("#conditionsListGroup").html("");
    for(i in conditions){
               
            let $element =  $('<li id="condition' + i + '" class="list-group-item list-group-item-action" value="' + i + '"></li>');
            $element.append('<span class="glyphicon glyphicon-unchecked" style="padding-right: 10px"></span>');

            //$element.html('<i class="bi bi-app" style="padding-right: 15px"></i>');
            $element.html($element.html() + conditions[i]);
    
            $element.on('click', function () {
                if($element.hasClass("list-group-item-success")){
                    $element.html($element.html().replace("glyphicon-check", "glyphicon-remove"));
                    $element.removeClass("list-group-item-success");
                    $element.addClass("list-group-item-danger");
                } else if($element.hasClass("list-group-item-danger")){
                    $element.removeClass("list-group-item-danger");
                    $element.html($element.html().replace("glyphicon-remove", "glyphicon-unchecked"));
                } else {
                    $element.html($element.html().replace("glyphicon-unchecked", "glyphicon-check"));
                    $element.addClass("list-group-item-success");
                }
            });
    
            $("#conditionsListGroup").append($element);
    }   

    // htmlCode += `</div>`;
    // document.getElementById("conditions").innerHTML= htmlCode;
}

function addNewCheckbox(i, expression){
    let $element =  $('<li id="condition' + i + '" class="list-group-item list-group-item-action" value="' + i + '"></li>');
    $element.append('<span class="glyphicon glyphicon-unchecked" style="padding-right: 10px"></span>');

    //$element.html('<i class="bi bi-app" style="padding-right: 15px"></i>');
    $element.html($element.html() + expression);

    $element.on('click', function () {
        if($element.hasClass("list-group-item-success")){
            $element.html($element.html().replace("glyphicon-check", "glyphicon-remove"));
            $element.removeClass("list-group-item-success");
            $element.addClass("list-group-item-danger");
        } else if($element.hasClass("list-group-item-danger")){
            $element.html($element.html().replace("glyphicon-remove", "glyphicon-unchecked"));
            $element.removeClass("list-group-item-danger");
        } else {
            $element.html($element.html().replace("glyphicon-unchecked", "glyphicon-check"));
            $element.addClass("list-group-item-success");
        }
    });

    $("#conditionsListGroup").append($element);
}

function updateChart(results){
    var j = 0;

    for(var i = options.min ; i<=options.max; i++){
        counts[j] = 0;
        j++;
    }

    for(i in results){
        year = results[i].date.substring(0, 4);
        if(true){
            counts[year-options.min]++;
        }

    }
}

/**
 * Creates the visual date slider by using configuration properties
 */
function initializeDateSlider(){
    let slider = document.getElementById('date-slider');
    
    noUiSlider.create(slider, {
        start: [options.initialMin, options.initialMax],
        connect: true,
        range: {
            'min': options.min,
            'max': options.max
        },
        step: options.step,
        tooltips: true,
        format: {
            to: function ( value ) {
              return value;
            },
            from: function ( value ) {
              return value.replace(',-', '');
            }
          },
    
        // Show a scale with the slider
        pips: {
            mode: 'steps',
            stepped: true,
            density: 4
        }
    });

    //When a min or max date is set on the range slider
    slider.noUiSlider.on('change.one', function () {
      updateBoxes(results, slider.noUiSlider.get()[0], slider.noUiSlider.get()[1]);
    });
}

/**
 * Prepare the resource box with shows the iri, the label, a comment related to the initial process resource
 * This function also manage the centering of the date filtering slider based on the initial resource date property value
 */
function initializeResourceDetails(iri, details){

    //Update ui values
    let resourceLink =   itemLink + iri.substring(iri.lastIndexOf("/") + 1);
    $("#resourceURL1").attr("href", resourceLink);
    $("#resourceURL2").attr("href", resourceLink);
    $("#resourceURI").html(iri);
    $("#resourceTitle").html(details["label"]);
    initialResourceType = details["type"];
    initialResourceTypeLabel = details["typeLabel"];

    if(details["comment"]){
        $("#infoBox").attr('title', details["comment"]).tooltip('_fixTitle');
    } else {
        let value = $("option:selected", "#languageSelect").val();
        $("#infoBox").attr('title', langData.resourceTooltip[value]).tooltip('_fixTitle');
    }

   
    //If the resource date has been retrieved, we update the slider to center the initial filter around this value
    if(details["date"]) {
        let year = parseInt(details["date"].substring(0, 4));
        options.initialMin = year - 5 ;

        options.initialMax = year + 5 ;
       // initializeDateSlider();
       
        let slider = document.getElementById('date-slider');
        slider.noUiSlider.updateOptions({
            start: [options.initialMin, options.initialMax]
        });
    }


}

/**
 * Prepare the list of properties to be selected for manual condition add
 * @param {*} properties 
 */
function preparePropertyAutocompleteValues(properties){
    inputProperties = {};
            
    for(i in properties){
        inputProperties[properties[i]] = i;
    }

    $('#propertyInput').autocomplete({
        source: inputProperties,
        maximumItems : 0,
        highlightClass: 'text-danger',
        treshold: 0,
      });
}

/**
 * Prepare the list of resources to be selected for manual condition add
 * @param {*} objects 
 */
function prepareObjectAutocompleteValues(objects){
    inputObjects = {};
            
    for(i in objects){
        inputObjects[objects[i]] = i;
    }

    $('#objectInput').autocomplete({
        source: inputObjects,
        maximumItems : 0,
        highlightClass: 'text-danger',
        treshold: 0,
      });
}

/**
 * Prepare the list of classes to be selected for filtering values in manual condition add
 * @param {*} types 
 */
function prepareTypesSelection(types){
    
    $('#resourceTypeSelect').html("");

    if(initialResourceType && initialResourceTypeLabel){ 
        //The select in the update initial resource modal
        $('#resourceTypeSelect').append($('<option>', { 
            value: initialResourceType,
            text : initialResourceTypeLabel
        }));
    }

    
    $.each(types, function (i, item) {
        $('#resourceTypeSelect').append($('<option>', { 
            value: i,
            text : item
        }));
    });

    //The select for the add manual condition modal
    $('#typesSelect').html("");

    $.each(types, function (i, item) {
        $('#typesSelect').append($('<option>', { 
            value: i,
            text : item
        }));
    });

}

/**
 * Refresh the advanced search data table based on user input
 * @param {*} results 
 */
function updateAdvancedSearchTable(results){

      var count = 1;
      var tableContent = [];
      var row;

      for (var key in results) {
        row = [];
        row.push(count);
        row.push(key);
        row.push(results[key])

        count++;
        tableContent.push(row);
      }

      $("#searchTable").dataTable().fnClearTable();

      if (tableContent.length != 0) {
        $("#searchTable").dataTable().fnAddData(tableContent);
        $("#searchTable tr").css("cursor", "pointer");
      }
}