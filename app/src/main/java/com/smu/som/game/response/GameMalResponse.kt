package com.smu.som.game.response

class GameMalResponse {

    public class GetMalMovePosition{
        var userId : Long
        var playerId : String
        var yutResult : String
        var newMalId : Int
        var malList : List<MalMoveInfo>

        constructor(userId:Long, playerId:String, yutResult: String, newMalId: Int, malList: List<MalMoveInfo>){
            this.userId = userId
            this.playerId = playerId
            this.yutResult = yutResult
            this.newMalId = newMalId
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

    public class MoveMalDTO{
        var userId : Long
        var playerId : String
        var malId : Int
        var point : Int
        var movement : List<Int>
        var nextPosition : Int
        var isEnd: Boolean
        var isCatchMal : Boolean
        var catchMalList : List<Int>
        var isUpdaMal : Boolean
        var updaMalId : Int

        constructor(userId: Long, playerId: String, malId: Int, point: Int, movement: List<Int>, nextPosition: Int, isEnd: Boolean, isCatchMal: Boolean, catchMalList: List<Int>, isUpdaMal: Boolean, updaMalId: Int){
            this.userId = userId
            this.playerId = playerId
            this.malId = malId
            this.point = point
            this.movement = movement
            this.nextPosition = nextPosition
            this.isEnd = isEnd
            this.isCatchMal = isCatchMal
            this.catchMalList = catchMalList
            this.isUpdaMal = isUpdaMal
            this.updaMalId = updaMalId
        }
    }
}