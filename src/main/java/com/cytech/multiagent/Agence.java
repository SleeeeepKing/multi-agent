package com.cytech.multiagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Agence extends Thread {
    public int id;

    private int position;
    private int final_position;
    // private int[] next={0,0,0,0};
    private int req=0;

    private int fatherchess=-1;
    private ArrayList<Integer> childrenchess=new ArrayList<>();

    private int give_way_position=-1;
    private int last_position=-1;
    private boolean allow=false;
    private int allow_id=0;
    private Map<Integer,Integer> request_position=new HashMap<>();
    private Map<Integer,Boolean> give_way_permit=new HashMap<>();
    public Agence(int id,int position,int final_position) {
        super();
        this.id = id;
        this.position=position;
        this.final_position=final_position;
    }

    @Override
    public void run() {
    /**
        int position = -1;
        for (int i = 0; i < 25; i++)
            if (MapChess.getInstant()[i] == this.id) {
                position = i;
                break;
            }
        if (position >= 0)
            MapChess.getInstant()[position] = 0;
        System.out.println(Arrays.toString(MapChess.getInstant()));*/
    }
    private int[] direction(){
        //根据当前位置和目标位置，计算可能行动的位置
        int[] direction={-1,-1,-1,-1};
        //归零，对应 上 下 左 右

        int position_x=this.position%5;
        int position_y=this.position/5;
        int final_position_x=this.final_position%5;
        int final_position_y=this.final_position/5;
        //计算当前和目标位置的行和列

        //当前与目标位置是否在同一行
        if(final_position_y-position_y!=0){
            if(final_position_y>position_y)
                //目标位置的行数大于当前位置的行数，则可以下移
                direction[1]=this.position+5;
            else
                //下移
                direction[0]=this.position-5;}
        if (final_position_x-position_x!=0){
            if(final_position_x>position_x)
                //右移
                direction[3]=this.position+1;
            else
                //左移
                direction[2]=this.position-1;
        }
        return direction;
    }
    /*
    * 该函数仅为初始移动的调用
    * 第一次调用时，如果自身可以不被阻碍的向目标方向移动则直接移动，否则需要请求让路
    * 但对于其它移动，如让路和等待让路后的移动，则需要另写逻辑
    * 因为让路和绕路的情况需要双向确认后再行动*/
    public boolean move(){
        //得到可能移动的位置

        int[] direction=this.direction();
        //初始化可能请求的agence
        int[] request_agence={0,0,0,0};
        for(int i=0;i<4;i++)
            //不为-1则可能移动
            if(direction[i]!=-1){
                //该位置没有棋子，则直接移动返回true
                if(MapChess.getInstant()[direction[i]]==0&&direction[i]!=this.last_position) {
                    MapChess.getInstant()[position] = 0;
                    MapChess.getInstant()[direction[i]] = this.id;
                    this.last_position=-1;
                    return true;
                }else
                    //有棋子阻挡，则记录
                    {
                    request_agence[i]=MapChess.getInstant()[direction[i]];
                    request_position.put(request_agence[i],direction[i]);
                }
            }
        //在函数没有结束的情况下，向所有可能代理发送请求
        for(int i=0;i<4;i++){
            if(request_agence[i]!=0)
            this.sendRequest(request_agence[i],this.position);
        }
        //等待消息，交由handrequest控制
        return false;
    }


    private boolean sendRequest(int id_request_to,int position){
        //将棋子id和其位置，发送给请求的棋子
        return  true;
    }
    private void handleRequest(){
        //从消息中获得棋子id和其位置
        int request_id=0;
        this.fatherchess=request_id;
        int position=0;
        int[] request_agence={-1,-1,-1,-1};
        int[] direction=this.direction();
        //第一种情况，让路的路径恰巧也在移动方向上且没有被阻塞，则允许让路
        for(int i=0;i<4;i++) {
            if (direction[i] != 0 && direction[i] != position) {
                if (MapChess.getInstant()[direction[i]] == 0) {
                    this.give_way_position=direction[i];
                    this.allow = true;
                    // 记录可让路的位置，向父棋子发送"回复请求"
                    // return;结束函数
                }else
                    request_agence[i] = MapChess.getInstant()[direction[i]];
                    //如果被阻塞,且阻塞棋子不是请求棋子,则记录该棋子id
            }
        }
        //this.req=0;
        for(int i=0;i<4;i++){
            if(request_agence[i]!=-1){
                this.req=+1;
                this.sendRequest(request_agence[i],this.position);
            }
        }
        //请求，等待，交由handlerequest处理
        if(this.req==0)
            this.allow=false;
        // 不允许，说明唯一的阻塞棋子也是请求棋子

    }
    private void informer(int id){
        //
    }
    private void handleinformer(){
        //是否接受让路应以informer形式通知请求棋子，
        //接受一个"回复请求通知"后，req参数-1
        String informertype="";

        if (informertype=="回复请求"){
        this.req=-1;
        //向this.give_way_permit中添加对方是否同意让路
        int id=0;
        boolean response=true;
        this.give_way_permit.put(id,response);
        if (this.req!=0)
        {
            //TODO
            //继续等待所有结果返回
             }
        else
        {
            //判断是否有结果为允许让路
            if (this.give_way_permit.containsValue(true)){

                for(Map.Entry<Integer, Boolean> entry :this.give_way_permit.entrySet()){
                    if(entry.getValue()){
                        this.allow_id=entry.getKey();
                        break;
                        }
                    }
                //得到一个可让路的子棋子id

                // TODO
                if(this.fatherchess>=0){
                    // 如果有父棋子，则先向父棋子发送"回复请求"，并等待
                }
                else{
                    // 如果没有父棋子，向其发送"确认选择通知"(有多个棋子可以让路时只选择一个)。并等待得到对方"移动完成通知"(对方让路径后，再移动)。
                }


                }
            else{

                if(this.fatherchess>=0)
                    this.Detour();
                    //如果没有父棋子实现
                    //绕路逻辑
                else
                    this.allow=false;
                //如果有父棋子，向父棋子发送拒绝的"回复通知"
            }

            }
        }
        if(informertype=="确认移动"){
            //收到确认移动，有两种情况，第一种可以直接让路，第二种让路路径被阻塞
            //因为让路路径被阻塞，才会发送让路请求，才会产生子棋子，所以以此作为判断依据
            if(this.childrenchess.isEmpty())
            {   
                //对于第一种，执行移动，然后向父棋子发出"移动完成通知"
            }else {
                //对于第二种，先向子棋子发出"确认移动通知"，然后等待
            }


        }
        if(informertype=="移动完成"){
            //收到"移动完成通知"后，执行移动，如果还有父棋子，则向其发送移动完成通知，没有则结束
            this.give_way_move();
            if(this.fatherchess>=0)
            {
                //通知父棋子
            }else{
                //没有父棋子，说明是初始棋子，已经完成移动，可以让下一个棋子行动
            }
        }
    }
    private void give_way_move(){
        if(childrenchess.isEmpty())
        {
            MapChess.getInstant()[this.give_way_position]=this.id;
            MapChess.getInstant()[this.position]=0;
        }
        if(this.allow_id!=0){
            MapChess.getInstant()[this.request_position.get(this.allow_id)]=this.id;
            MapChess.getInstant()[this.position]=0;
        }
        this.reset_variable();
    }
    private void Detour(){
        int[] direction=this.direction();
        int next_position=-1;
        for(int i=0;i<4;i++){
            //绕路也不走回头路
            if(direction[i]==0&&direction[i]!=this.last_position){
                switch (i) {
                    case 0 -> next_position = this.position - 5;
                    case 1 -> next_position = this.position + 5;
                    case 2 -> {
                        if (this.position % 5 != 0)
                            next_position = this.position - 1;
                    }
                    case 3 -> {
                        if ((this.position + 1) % 5 != 0)
                            next_position = this.position + 1;
                    }
                }
                if(next_position>=0&&next_position<=24)
                    break;
            }
        }
        // 绕路，并记录当前位置，避免下次行动回到该位置
        this.last_position=this.position;
        MapChess.getInstant()[this.position]=0;
        MapChess.getInstant()[next_position]=this.id;
    }
    private void reset_variable(){
        this.fatherchess=-1;
        this.childrenchess=new ArrayList<>();
        this.give_way_position=-1;
        this.allow=false;
        this.allow_id=0;
        this.request_position=new HashMap<>();
        this.give_way_permit=new HashMap<>();
    }
}
