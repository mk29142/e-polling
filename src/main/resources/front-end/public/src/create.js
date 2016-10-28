(function() {

  var boxOptions = {
    id: id++,
    parentId: null,
    type: 'Issue'
  };

  new Box(boxOptions);

  list.push(boxOptions);

  $('#submit-poll').click(function() {
    for(var i = 0; i < list.length; i++) {
      var elem = list[i];
      elem.value = $('#box' + elem.id).find('input').val();
    }
  });
})();
