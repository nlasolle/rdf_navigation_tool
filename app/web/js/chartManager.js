var ctx;
var myChart = null;
var counts = [];
var years = [];

//Get chart title with right language
var i= $("option:selected", this).val();
//var chartLabel = langData.chartLabel[i];

function prepareModalChart(min, max){
    years = [];
   for(var i = min; i<max; i++){
        years.push(i);
   }
}

$('#chartModal').on('shown.bs.modal', function () {

    if (myChart != null) {
        myChart.destroy();
    }

    
    ctx = document.getElementById('lettersChart').getContext('2d');

    myChart = new Chart("lettersChart", {
        type: 'bar',

        data: {
            labels: years,
            datasets: [{
                label: "Distribution",
                data: counts,
                backgroundColor: 'rgba(110, 211, 155, 0.5)',
                borderWidth: 1
            }]
        },
        options: {
            maintainAspectRatio: true,
            responsive: true,
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });

   
    myChart.render();
});