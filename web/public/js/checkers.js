dojo.require('dojo.dnd.Source');

dojo.declare('SquareSource', dojo.dnd.Source, {
  // don't allow multiple selection regardless of key state
  singular: true,
  
  // always sync up node list with square contents
  autoSync: true,
  
  // should contain a mapping for each legal move, such that:
  //   this.play_map['2,2']['3,3'] <==> true
  // if the move from 2,2 => 3,3 is allowed
  play_map: {},
  
  constructor: function(node, params) {
    // keep track of the coords managed by this square, so we can test for it in checkAcceptance
    this._coords = params.coords;
  },
  
  copyState: function() {
    // don't allow pieces to be copied regardless of key state
    return false;
  },
  
  checkAcceptance: function(source) {
    // return whether the piece wanting to be dropped on us represents a valid move
    return (this.play_map[source._coords] || dojo.dnd._empty)[this._coords];
  }
});

dojo.addOnLoad(function() {
  var piece_images = {
     '1': '/checkers/s/images/pb.png',
     '2': '/checkers/s/images/kb.png',
    '-1': '/checkers/s/images/pr.png',
    '-2': '/checkers/s/images/kr.png'
  };
  var xhr = dojo.xhrGet({
    url: '/checkers/new',
    handleAs: 'json'
  });
  xhr.addCallback(function(res) {
    // create the board and set up targets
    var board = dojo.byId('board');
    board.innerHTML = '';
    
    for (var y = 7; y >= 0; y--) {
      // create the table row for this row
      var tr = dojo.create('tr', {}, board);
      for (var x = 0; x <= 7; x++) {
        // create the table cell for this square
        if ((x + y) % 2 == 0) {
          var td = dojo.create('td', {'class': 'on'}, tr);
          // look up piece image and insert image tag
          var p = res.board[y][x];
          var src = piece_images[p];
          if (src) dojo.create('img', {'src': src, 'class': 'dojoDndItem'}, td);
          // create the drag/drop source
          new SquareSource(td, {coords: [x, y].toString()});
        } else {
          // create non-playable square
          dojo.create('td', {'class': 'off'}, tr);
        }
      }
    }
  });
  xhr.addCallback(function(res) {
    // set up play_map based on moves from server
    for (var i = 0; i < res.plays.length; i++) {
      var play = res.plays[i];
      var from = SquareSource.prototype.play_map[play[0]] = {};
      for (var j = 1; j < play.length; j++) {
        from[play[j][0]] = true;
      }
    }
  });
});
