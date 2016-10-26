(function() {
  var issues = [{
    id: 0,
    type: 'issue',
    text: 'The big issue',
    parent: null
  }, {
    id: 1,
    type: 'issue',
    text: 'The bigger issue',
    parent: null
  }, {
    id: 2,
    type: 'issue',
    text: 'The biggest issue',
    parent: null
  }];

  function setIssueList() {
    var issueList = $('#issue-list');

    for (var i = 0; i < issues.length; i++) {
      issueList.append('<a class="collection-item"' +
      'value="' +
      issues[i].id +
      '">' +
      issues[i].text +
      '</a>');
    }
  };

  setIssueList();
})();
