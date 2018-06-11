var map;
//draw map through google map api
 function initMap() {
            var uluru = {
                lat: 0,
                lng: 0
            };
		map = new google.maps.Map(document.getElementById('map'), {
                zoom: 4,
                center: uluru
            });
//when you click the button you can see marker on google map
 $('#getData').click(function (event) {
        console.log('button clicked');
        receiveData();
    });
       
}
//receive json data from server
function receiveData() {
    $.ajax({
        type: 'GET',
        url: '/getData',
        dataType: 'JSON',
        success: function (data) {
            console.log('get data success');
            console.log(data);
		drawMarker(data);	
        },
        error: function (request, status, error) {
            console.log('request : ' + request);
            console.log('status : ' + status);
            console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + error);
        }
    });
}
//draw marker on google map
function drawMarker(location){
	for(var i = 0; i<location.length;i++){
		console.log(location[i]);
		var latitude = location[i].latitude;
		var longitude = location[i].longitude;
		var latLng = new google.maps.LatLng(latitude, longitude);
		var marker = new google.maps.Marker({
			position : latLng,
			map : map
		});
	}

}



