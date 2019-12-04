var temp = {}
var high = 0
var low = 0
$(function() {

  $('#datepicker').datepicker({
    date: new Date(),
    format: "dd.mm.yyyy"
  });
  $('#datepicker').datepicker("pick")
  $('#result').hide()
  $('#error').hide()

  $('#search').click(function() {
    var date = new Date();
    date.setFullYear($('#datepicker').val().split(".")[2])
    date.setMonth($('#datepicker').val().split(".")[1]-1)
    date.setDate($('#datepicker').val().split(".")[0])
    date.setHours(0)
    date.setMinutes(0)
    date.setSeconds(1)
    var date2 = new Date();
    date2.setFullYear($('#datepicker').val().split(".")[2])
    date2.setMonth($('#datepicker').val().split(".")[1]-1)
    date2.setDate($('#datepicker').val().split(".")[0])
    date2.setHours(23)
    date2.setMinutes(59)
    date2.setSeconds(59)
    $.get("/feinstaub/" + date.valueOf() + "/" + date2.valueOf(), function(data) {
      try {
        var temp = JSON.parse(data);
        temp.labels.reverse();
        temp.data1.reverse();
        temp.data2.reverse();
        console.log(temp);
        var ctx = "myChart";
        var chart = new Chart(ctx, {
          "type": "line",
          "data": {
            labels: temp.labels,
            datasets: [{
          label: "PM2.5 in µg/m³",
          data: temp.data1,
          pointRadius: 0,
          borderWidth: 1
        },{
          label: "PM10 in µg/m³",
          data: temp.data2,
          pointRadius: 0,
          borderWidth: 1
        }]
          }
        })
        $('#error').hide()
        $('#result').show()
      } catch {
        $('#result').hide()
        $('#error').show()
      }
    });

  })

})
