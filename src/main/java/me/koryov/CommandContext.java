package me.koryov;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;


public class CommandContext {


    private final Player sender;

    private final String[] args;



    public CommandContext(Player sender, String[] args) {

        this.sender = sender;
        this.args = args;

    }



    public Player getSender() {

        return sender;

    }




    public boolean has(int index) {

        return index >= 0 && index < args.length;

    }




    public String get(int index) {

        return args[index];

    }




    public int getInt(int index, int fallback) {


        if (!has(index)) {
            return fallback;
        }


        try {

            return Integer.parseInt(args[index]);

        } catch (Exception e) {

            return fallback;

        }

    }





    /*
     * Lấy target đầu tiên
     *
     * Không có target = bản thân
     */
    public Player getTargetOrSelf() {


        if (!has(0)) {

            return sender;

        }



        Player target =
                Bukkit.getPlayerExact(
                        args[0]
                );


        if (target == null) {

            return sender;

        }


        return target;

    }




    /*
     * Target bắt buộc
     */
    public Player getTarget() {


        if (!has(0)) {

            return null;

        }


        return Bukkit.getPlayerExact(
                args[0]
        );

    }





    public Material getMaterial(int index) {


        if (!has(index)) {

            return null;

        }


        return Material.matchMaterial(
                args[index]
        );

    }

    public int length() {

        return args.length;

    }
    public String[] getArgs() {

        return args;

    }
    public String join(int start) {

        StringBuilder builder = new StringBuilder();

        for (int i = start; i < args.length; i++) {

            builder.append(args[i]);

            if (i != args.length - 1) {
                builder.append(" ");
            }

        }

        return builder.toString();

    }
}