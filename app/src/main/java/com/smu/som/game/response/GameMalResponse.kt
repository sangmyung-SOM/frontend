package com.smu.som.game.response

class GameMalResponse {

    public class GetMalMovePosition{
        var userId : Long
        var playerId : String
        var yutResult : String
        var malList : List<MalMoveInfo>

        constructor(userId:Long, playerId:String, yutResult: String, malList: List<MalMoveInfo>){
            this.userId = userId
            this.playerId = playerId
            this.yutResult = yutResult
            this.malList = malList
        }
    }

    public class MalMoveInfo{
        var malId : Int
        var isEnd: Boolean
        var point : Int
        var position : Int
        var nextPosition : Int

        constructor(malId: Int, isEnd : Boolean, point : Int, position : Int, nextPosition: Int){
            this.malId = malId
            this.isEnd = isEnd
            this.point = point
            this.position = position
            this.nextPosition = nextPosition
        }
    }
}