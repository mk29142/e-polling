(function() {
  let pollId = window.location.href.substring(
    window.location.href.lastIndexOf('/') + 1);

  $.get('/boxes/' + pollId, function(data) {
    let questions = JSON.parse(data);

    let title = $('#debate-title');
    let question = $('#question');
    let options = $('input[name=options]');
    let reason = $('#opinion-area');
    let issue = questions[0];
    let counter = 0;
    let currQ = questions[counter];

    let userId = '';
    // Do a post to /user so we are able to log their ip address once to put questions in the database
    $.ajax({
      type: 'POST',
      url: '/user/' + pollId,
      data: {
        userId: userId,
      },
      success: function(data) {
        console.log('id: ' + data);
        userId = data;
      },
      error: function(data) {
        console.log('Error in user post: ', data);
      },
    });

    let currConflictSet;
    let isArgSupported;

    let dynamicData = {
      questions: questions, // questions to update answers for,
                     // initialised to be all questions with head as first value
      nextLevel: 0,  // next level to be searched for inconsistencies
      userId: userId
    };

    $('#finalQ').hide();
    title.html(issue.text);
    let num = parseInt(currQ.id) + 1;
    question.html(num + ': ' + currQ.text);
    setNavList();
    setActive([counter]);

    $('input[name="options"]').change(function(e) {
      e.preventDefault();
      setTimeout(changeQuestion, 500);
    });

    $('#nextQ').click(function(e) {
      e.preventDefault();
      changeQuestion();
    });

    $('#finalQ').click(function(e) {
      e.preventDefault();
      currQ.support = options.filter(':checked').val();
      currQ.reason = reason.val();

      if (currQ.support) {
        $('#vote-yes').prop('checked', currQ.support === 'yes');
        $('#vote-no').prop('checked', currQ.support === 'no');
      } else {
        $('#vote-yes').prop('checked', false);
        $('#vote-no').prop('checked', false);
      }

      if (allAnswered(questions)) {
        // Send this ajax post for first answers and receive inconsistencies from first level
        submitDynamicData();
      }

    });

    // A modal will pop up with dynamic questions from data obj
    // on last round of dynamic questions modal will show submit
    // window.location.href = '/results/' + data;
    $('#dynamicQuestionSubmit').click(function() {
      let index = findCurrConflictIndex();

      let childNum = 1;
      for (let i = 0; i < dynamicData.questions.length; i++) {
        // So that we are only looking at children of the conflicting one
        if (parseInt(dynamicData.questions[i].parent) === index) {
          if (isArgSupported && dynamicData.questions[i].type === 'Pro' ||
          !isArgSupported && dynamicData.questions[i].type === 'Con') {
            let checked = $('#q' + childNum + '-yes').is(':checked')? 'yes' : 'no';
            dynamicData.questions[i].support = checked;
            childNum++;
          }
        }
      }

      // If there was one, then we need to add it.
      addDynamicArgument(index);
    });

    // This argument must be added as a child to the parent argument
    function addDynamicArgument(index) {
      let type = isArgSupported? 'Pro' : 'Con';
      let text = $('#dynamicQuestionReason').val();

      if (text) {
        $.ajax({
          type: 'POST',
          url: '/useradded/' + pollId,
          data: JSON.stringify({
            parent: index,
            text: text,
            type: type,
            support: 'yes'
          }),
          dataType: 'json',
          success: function(data) {
            console.log(data);
            if (data === 'SUCCESS') {
              // Reset to nothing for the next question
              $('#dynamicQuestionReason').val('');

              // Push this dummy node which is not to be kept
              dynamicData.questions.push({
                id: 1000000,
                parent: index,
                text: text,
                type: type,
                support: 'yes'
              });
              // Send this ajax post when we want inconsistencies for next level
              submitDynamicData();
            }
          },
          error: function() {
            console.log('Error submitting the new argument');
          }
        });
      } else {
        submitDynamicData();
      }
    }

    function findCurrConflictIndex() {
      let result = 0;
      let headQ = currConflictSet.dynamicQuestions[0];
      for (i = 0; i < dynamicData.questions.length; i++) {
        if (dynamicData.questions[i].text === headQ.text) {
          result = i;
          break;
        }
      }

      return result;
    }

    $('#nav-list a').click(function(e) {
      counter = parseInt(e.currentTarget.text) - 2;
      changeQuestion();
    });

    /*
     * We want dynamic questions to be a list of nodes with one head node followed by its supporters/attackers
     * and we want the Box object to contain a vote field to make figuring out what type of dyanmic q
     */
    function submitDynamicData() {
      dynamicData.userId = userId;
      $.ajax({
        type: 'POST',
        url: '/answers/' + pollId,
        data: JSON.stringify(dynamicData),
        dataType: 'json',
        success: function(data) {
          if (data !== 'STOP') {
            currConflictSet = data;
            displayModal();
          } else {
            window.location.href = '/results/' + pollId;
          }
        },
        error: function() {
          console.log('Error in submitting dynamic data');
        }
      });
    }

    function setNavList() {
      let nav = $('#nav-list');

      for (let i = 0; i < questions.length; i++) {
        let num = parseInt(questions[i].id) + 1;
        nav.append('<li class="waves-effect"><a ' +
        'value="' + questions[i].id +
        '">' + num + '</a></li>');
      }
    }

    function allAnswered(questions) {
       let unansweredIndices = [];

       for (let i = 0; i < questions.length; i++) {
         if (questions[i].support === undefined) {
           unansweredIndices.push(i);
         }
       }

       // Highlight all the wrong questions in red
       setActive(unansweredIndices);
       return unansweredIndices.length === 0;
    }

    /*
     * Retrieve current conflict index if user if pro conflict index:
     * show supporters and let user change answer for them to be pro
     * allow user to change answer
     * allow user to add supporting argument
     */
    function displayModal() {
      $('#questions').html('');

      let qs = currConflictSet.dynamicQuestions; // For ease
      let headQ = qs[0];

      isArgSupported = headQ.vote === 'For'
      let conflictText = isArgSupported?
        'You voted for the argument but against all of its supporting arguments.' :
        'You voted against the argument but against all of its attacking arguments.';
      let supportOrAttack = isArgSupported? 'Supporting arguments:' :  'Attacking arguments:'
      let sOrA = isArgSupported? 'a supporting' : 'an attacking';

      $('#conflictTitle').html('CONFLICT! Your answer to "' +
        headQ.text + '" is inconsistent with your other answers! <br>' +
        conflictText + ' Please edit your answers below:');

      let support = qs[0].vote === 'For'? 'yes' : 'no';
      createQuestion(qs[0].text, 0);
      $('#q' + 0 + '-' + support).prop('checked', true);

      let supOrAtt = isArgSupported ? "Supporters:" : "Attackers:";

       $('#' + qs[0].text).append("<br><br>"+supOrAtt);

      for (let i = 1; i < qs.length; i++) {
        let support = qs[i].vote === 'For'? 'yes' : 'no';
        createQuestion(qs[i].text, i);
        $('#q' + i + '-' + support).prop('checked', true);
      }

      $('#questions').append('<br> Or add ' + sOrA + ' argument that was not mentioned:');

      $('#dynamicModal').openModal({
        dismissible: false,
      });
    }

    // At the moment, we are making all red unanswered questions lose redness
    // on any question click, we want the rest to stay red until answered
    function setActive(indices) {
      let nav = $('#nav-list');
      let children = nav.children();
      children.removeClass('active');

      indices.forEach(function(index) {
        let active = children.eq(index);
        active.addClass('active');   
      });
    }

    function changeQuestion() {
      currQ.support = options.filter(':checked').val();
      currQ.reason = reason.val();

      if (counter > questions.length - 2) {
        return;
      }

      currQ = questions[++counter];
      var num = parseInt(currQ.id) + 1;
      question.text(num + ': ' + currQ.text);
      reason.val(currQ.reason);

      if (currQ.support) {
        $('#vote-yes').prop('checked', currQ.support === 'yes');
        $('#vote-no').prop('checked', currQ.support === 'no');
      } else {
        $('#vote-yes').prop('checked', false);
        $('#vote-no').prop('checked', false);
      }

      if (currQ.id === questions.length - 1) {
        $('#nextQ').hide();
        $('#finalQ').show();
      } else {
        $('#nextQ').show();
        $('#finalQ').hide();
      }

      setActive([counter]);
    }

    function createQuestion(question, counter) {
      let q = '<div id="' + question + '">' +
      '<p>' + question + '</p>' +
      '<input type="radio" id="q' + counter + '-yes" name="' + counter + '" value="yes">' +
      '<label for="q' + counter +'-yes">Yes</label>   &nbsp; &nbsp; &nbsp;  ' +
      '<input type="radio" id="q' + counter + '-no" name="' + counter + '" value="no">' +
      '<label for="q' + counter +'-no">No</label>' +
      '</div>';

      $('#questions').append(q);
    }
  });
})();
