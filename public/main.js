var socket; // Socket connection
var playerId;
var queryParam;
var queryParam2;
var queryParam3;
var queryParam4;
var queryParam5;
var isConnected = false;
var socket = new Socket();
var isSubscribed = false;
var roomCode;
window.socket = socket;

$(document).ready(function() {
  liarsDice.init();
});
var liarsDice = {

  init: function() {
    liarsDice.events();
  },
  events: function () {
    $('form').on('submit',function(event) {
      event.preventDefault();
    })
    $('.learn').on('click', function(event){
      event.preventDefault();
      console.log("you clicked learn to play");
      $('.realRules').removeClass('inactive');
      $('.rulesPage').addClass('inactive');
    });

    $('.continue').on('click', function(event){
      event.preventDefault();
      console.log("you clicked continue");
      $('.homePage').removeClass('inactive');
      $('.rulesPage').addClass('inactive');
    });

    $('.gotIt').on('click', function(event){
      event.preventDefault();
      console.log("you clicked got it");
      $('.homePage').removeClass('inactive');
      $('.realRules').addClass('inactive');
    });
    $('#questionMark').on('click', function(event){
      event.preventDefault();
      console.log("you clicked question");
      $('h4').removeClass('hide');
      $('#questionMark').addClass('hide');
    });

    $('body').on('click','.abandonShip', function(event){
      console.log("RELODA!")
      window.location.reload();
    });

    $('.submit').on('click', function(event){
      event.preventDefault();
      console.log("you clicked submit");

      var name = $('input[name="name"]').val();
      roomCode = $('input[name="roomCode"]').val();
      if (roomCode === "") {
        roomCode = "undefined";
      } else {
         roomCode = roomCode
      };
      // var onPlayerList = function(playerList) {
      //   _.each()
      //   $('.nameContent').html("");
      // };

      $('.lobby').removeClass('inactive');
      $('.homePage').addClass('inactive');
      socket.connectSocket(name,roomCode);
    //  window.player = function() {
    //    socket.getPlayerList();
    //  }
      // socket.sendFirstConnection();
    });


    $('.box').on('click', function(event){
      // var names = $('.nameContent').html();
      socket.playRollDie();

      setTimeout(function() {
        rollDicePage(window.diceToDisplay)
      },2000);

      event.preventDefault();

      console.log("you clicked start");
      $('.bigSection').removeClass('inactive');
      // $('.nameContent').html(names);
      $('.lobby').addClass('inactive');
      $('.title').css('margin-top',"1%");
      socket.getPlayerList();
// cup that lifts and disapears
      $('.cup2').stop(true, true).delay(2100).animate({
        marginTop: -1000
      }, 1200);
    });

// Submit a wager
    $('.submitDice').on('click', function(event){
      $(this).trigger( "blur" );
      event.preventDefault();
      console.log("you clicked dice submit");
      socket.raiseStake();
   });
// submit bs
   $('.bluff').on('click', function(event){
     event.preventDefault();
     console.log("you clicked Bull Shit");
     socket.callBullShit();
   });
// start next round and rerequest dice
  $('.reroll').on('click', function(event){
    event.preventDefault();
    $('.bigSection').removeClass('inactive');
    $('.loser').addClass('inactive');
    console.log("you rerolled");
    // getDiceBack();
    socket.playRollDie()

    setTimeout(function() {
      for (var i = 1; i < 99999; i++) {
        window.clearInterval(i);
      }
      rollDicePage(window.diceToDisplay)
    },4000);
  });

  $('.seeRulers').on('click', function(event){
    event.preventDefault();
    $('.realRulers').removeClass('inactive');
    $('.bigSection').addClass('inactive');
    console.log("you want rules");
  });

  $('.gotItz').on('click', function(event){
    event.preventDefault();
    $('.bigSection').removeClass('inactive');
    $('.realRulers').addClass('inactive');
    console.log("back to game");
  });

  $('body').on('click', '.error', function(event){
    event.preventDefault();
    $('.error').removeClass('yes');
    $('.error').addClass('no');
  });

    // press space bar to view dice
    $(window).keydown(function(e) {
      if (e.which === 32) {
        $('.cup').animate({
          opacity: .2
        }, 10);
      }
    });
    // returns to main view when space bar is released
    $(window).keyup(function(e) {
      if (e.which === 32) {
        $('.cup').animate({
          opacity: 1
        }, 10);
      }
    });
  },
}

function Socket() {
  var playerId;
  var _this = this;
  var socketInternal;

  _this.connectSocket = function(name,roomCode) {
    var ws = new SockJS("/liarsDice")
    _this.socketInternal = Stomp.over(ws)
    _this.socketInternal.connect({name:name, roomCode: roomCode}, function() {
      _this.onSocketConnected()
    })
  };

  _this.onSocketConnected = function() {
    var url = _this.socketInternal.ws._transport.url.split("/");
    playerId = url[url.length-2];

    isConnected = true

      //returns your player info
      _this.socketInternal.subscribe("/topic/lobby/" + playerId, getDiceBack)
      //returns errors from server
      _this.socketInternal.subscribe("/topic/lobby/error/" + playerId, errorFromServer);



      //
      // function test1(data) {
      //   console.log("SUBSCRIBE TOPIC LOBBY", data);
      //
      // }
      // function test2(data) {
      //   console.log("SUBSCRIBE TOPIC playerlist",data);
      //
      // }  function test3(data) {
      //     console.log("SUBSCRIBE TOPIC LOSER",data);
      // }


      _this.socketInternal.send("/app/lobby/" + playerId, {}, "");





    };
    _this.getPlayerList = function() {
      console.log("this is the player list", playerId);
      _this.socketInternal.send("/app/lobby/JoinGame/" + roomCode,{} ,playerId);
    }

    _this.resetGame = function(roomCode){
      console.log("reset the game", playerId);
      _this.socketInternal.send("/app/lobby/resetGame/" + roomCode, {}, playerId);
    }

    _this.playRollDie = function() {
      console.log("THIS IS A PLAYER ID IN ROLLDIE", playerId);
      _this.socketInternal.send("/app/lobby/rollDice/" + roomCode, {}, playerId);
    }

    _this.callBullShit = function() {
      console.log("BULL SHIT", playerId);
      _this.socketInternal.send("/app/lobby/callBluff/" + roomCode, {}, playerId);
    }

    _this.raiseStake = function(){
      var quantity = parseInt($('input[name="quantity"]').val());
      var quality = parseInt($('input[name="quality"]').val());
      var stake = {
           "playerId": playerId,
           "newStake": [quantity, quality]
         }
         $('input[name="quantity"]').val("");
         $('input[name="quality"]').val("");
     _this.socketInternal.send("/app/lobby/setStake/" + roomCode,{}, JSON.stringify(stake));
    }

  // connectSocket();
    _this.sendFirstConnection = function(thingId) {
      _this.socketInternal.send("/app/lobby/" + thingId, {}, "");
    }


}

function youLost(data){
      var parsed = JSON.parse(data.body);
      var player;
      console.log("LOSER", data)
      $('.bigSection').addClass('inactive');
      $('.loser').removeClass('inactive');
      player = parsed.name + ' lost!';
      $('.nameLoser').html(player);
      _.each(data, function onPlayerList(data) {
      })
    }

    function playerList(data) {
      var parsed = JSON.parse(data.body);
      var stake, score, player;
      var content = '';
      var code = '';
      console.log("PLAYER LIST", parsed);
      code += "<p>Send friends your Room Code: "
            + parsed.playerList.playerDtos[0].roomCode
            + '</p>'
      $('.roomNumber').html(code);

      // var players = parsed.playerList.playerDtos
      // console.log("PLAYERLIST", players);
      // _.each(data, function onPlayerList(data) {

      if($('.lobby').hasClass('inactive')){
        if(parsed.playerList.playerDtos.length > 0){
          parsed.playerList.playerDtos.forEach(function(el){
            if(el.stake) {
            console.log(el);
            if(parsed.gameState.activePlayerSeatNum === el.seatNum){
              content += '<li class="activePlayer">'
            } else {
              content += '<li>'
            }
            content +=  el.name
                        + '<ul><li>Score: '
                        + el.score
                        + '</li><li>Stake: '
                        + el.stake[0]
                        + ', '
                        + el.stake[1]
                        + "'s</li></ul>"
                        + '</li>'
            } else {
              if(parsed.gameState.activePlayerSeatNum === el.seatNum){
                content += '<li class="activePlayer">'
              } else {
                content += '<li>'
              }
              content +=  el.name
                          + '</li>'
                          + '<ul><li>Score: '
                          + el.score
                          + '</li></ul>'
            }
          })
          $('.nameContent > ul').html(content);

        }
      } else if($('.bigSection').hasClass('inactive')){
        if(parsed.playerList.playerDtos.length > 0){
          parsed.playerList.playerDtos.forEach(function(el){
            console.log(el);
            content += '<li>' + el.name + '</li>'

          })
          $('.nameContent > ul').html(content);
        }
      }


    }



function rollDicePage(diceObject) {

  // spinCount=how many times the dice spins before it lands on the number
  // dice one
      var faceOne = 1;
      var spinCount = 0;
      var currentSpinCount = 0;
      var showOne = function() {
        $('#cube').attr('class', 'showOne' + faceOne);
        if (faceOne == 6) {
          faceOne = 1;
        } else {
          faceOne++;
        }
        if (currentSpinCount == spinCount) {
          return faceOne = diceObject.queryParam;
        }
        currentSpinCount++;
      };
      var timer1 = setInterval(showOne, 500);
      // dice two
      var faceTwo = 1;
      var spinCount2 = 2;
      var currentSpinCount2 = 0;
      var showTwo = function() {
        $('#cube2').attr('class', 'showTwo' + faceTwo);
        if (faceTwo == 6) {
          faceTwo = 1;
        } else {
          faceTwo++;
        }
        if (currentSpinCount2 == spinCount2) {
          return faceTwo = diceObject.queryParam2;
        }
        currentSpinCount2++;
      };
      var timer2 = setInterval(showTwo, 500);
      // third dice
      var faceThree = 1;
      var spinCount3 = 2;
      var currentSpinCount3 = 0;
      var showThree = function() {
        $('#cube3').attr('class', 'showThree' + faceThree);
        if (faceThree == 6) {
          faceThree = 1;
        } else {
          faceThree++;
        }
        if (currentSpinCount3 == spinCount3) {
          return faceThree = diceObject.queryParam3;
        }
        currentSpinCount3++;
      };
      var timer3 = setInterval(showThree, 500);
      // fourth dice
      var faceFour = 1;
      var spinCount4 = 1;
      var currentSpinCount4 = 0;
      var showFour = function() {
        $('#cube4').attr('class', 'showFour' + faceFour);
        if (faceFour == 6) {
          faceFour = 1;
        } else {
          faceFour++;
        }
        if (currentSpinCount4 == spinCount4) {
          return faceFour = diceObject.queryParam4;
        }
        currentSpinCount4++;
      };
      var timer4 = setInterval(showFour, 500);
      // fifth dice
      var faceFive = 1;
      var spinCount5 = 2;
      var currentSpinCount5 = 0;
      var showFive = function() {
        $('#cube5').attr('class', 'showFive' + faceFive);
        if (faceFive == 6) {
          faceFive = 1;
        } else {
          faceFive++;
        }
        if (currentSpinCount5 == spinCount5) {
          return faceFive = diceObject.queryParam5;
        }
        currentSpinCount5++;
      };
      var timer5 = setInterval(showFive, 500);
}

function getDiceBack(data) {
  window.preStuff = data;
  console.log("GET DICE BACK", data);
  data = JSON.parse(data.body);
  window.glob = data;
  // console.log("SHOW DATA DICE", data.dice);
  if(data.dice) {
    var diceRol = {
        queryParam: data.dice[0],
        queryParam2: data.dice[1],
        queryParam3: data.dice[2],
        queryParam4: data.dice[3],
        queryParam5: data.dice[4],
      };
      window.diceToDisplay = diceRol
      return diceRol
  }
  playerId = data.id;
  roomCode = data.roomCode;

  if(roomCode && !isSubscribed){
    socket.socketInternal.subscribe("/topic/playerList/" +roomCode, playerList);
    socket.socketInternal.subscribe("/topic/loser/" + roomCode, youLost);
    socket.socketInternal.send("/app/lobby/JoinGame/" + roomCode,{} ,playerId);
    isSubscribed = true;
  }


}

function errorFromServer(message) {
    console.log(message);
    // alert("That was not a valid play\nor it's not your turn.\nPlease try again.");
    $('.error').addClass('yes');
    $('.error').removeClass('no');
    // $('.error').html('That was not a valid play or it may not be your turn. Please try again or revisit the rules.');
}
