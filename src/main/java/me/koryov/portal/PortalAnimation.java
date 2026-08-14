package me.koryov.portal;

public class PortalAnimation {

    public static float tick(

            float current,

            float target,

            float speed

    ){

        if(current<target){

            current=Math.min(

                    target,

                    current+speed

            );

        }

        else if(current>target){

            current=Math.max(

                    target,

                    current-speed

            );

        }

        return current;

    }

}