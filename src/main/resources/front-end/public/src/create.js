(function() {
  var boxOptions = {
    id: id++,
    parentId: null,
    type: 'Issue',
    level: 0
  };

  new Box(boxOptions);

  list.push(boxOptions);

  $('#submit-poll').click(function() {
    for (var i = 0; i < list.length; i++) {
      var elem = list[i];
      elem.value = $('#box' + elem.id).find('input').val();
      var oldParent = elem.parentId;
      elem.parentId = oldParent ? parseInt(oldParent.substring(3)) : null;
    }

    var data = {
      name: list[0].value,
      list: list
    };

    $.ajax({
      type: 'POST',
      url: '/create',
      data: JSON.stringify(data),
      dataType: 'json',
      success: function(data) {
        console.log(data);
        window.location.href = '/results/' + data;
      }
    });
  });
})();
