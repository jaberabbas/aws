    $(function() {

    $("#bar").hide()


   } );

  function ProcessImages() {

    //Post the values to the controller
    $("#bar").show()
    var email =  $('#email').val();

    $.ajax('/report', {
        type: 'POST',  // http method
        data: 'email=' + email ,  // data to submit
        success: function (data, status, xhr) {

            $("#bar").hide()
             alert(data) ;
        },
        error: function (jqXhr, textStatus, errorMessage) {
            $('p').append('Error' + errorMessage);
        }
    });
    }

    function DownloadImage(){

     //Post the values to the controller
     var photo =  $('#photo').val();
     window.location="../downloadphoto?photoKey=" + photo ;
    }
