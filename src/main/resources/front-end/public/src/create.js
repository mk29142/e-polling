(function() {
  let boxOptions = {
    id: id++,
    parentId: null,
    type: 'Issue',
    level: 0,
  };

  new Box(boxOptions);

  list.push(boxOptions);

  $('#submit-poll').click(function() {
    for (let i = 0; i < list.length; i++) {
      let elem = list[i];
      elem.value = $('#box' + elem.id).find('input').val();
      let oldParent = elem.parentId;
      elem.parentId = oldParent ? parseInt(oldParent.substring(3)) : null;
    }

    let data = {
      name: list[0].value,
      list: list,
    };

    $.ajax({
      type: 'POST',
      url: '/create',
      data: JSON.stringify(data),
      dataType: 'json',
      success: function(data) {
        console.log(data);
        window.location.href = '/results/' + data;
      },
    });
  });
})();
