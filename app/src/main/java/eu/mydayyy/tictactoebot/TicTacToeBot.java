package eu.mydayyy.tictactoebot;

import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static eu.mydayyy.tictactoebot.Util.delay;
import static eu.mydayyy.tictactoebot.Util.sendLocalNotification;


public class TicTacToeBot implements IXposedHookLoadPackage {
    private Context context;
    private ClassLoader classLoader;

    private Object mainScreenGameContext;
    private Object currentGameEngine;
    private Object currentActionResolver;

    private long id = 0;

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if(!lpparam.packageName.equals("it.megasoft78.trispad.android")) {
            return;
        }

        classLoader = lpparam.classLoader;

        XposedHelpers.findAndHookMethod("it.megasoft78.trispad.android.AndroidLauncher", lpparam.classLoader, "onResume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                context = (Context) param.thisObject;
            }
        });

        XposedHelpers.findAndHookMethod("it.megasoft78.trispad.android.AndroidLauncher", lpparam.classLoader, "openScreenOnCompleted", byte.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if((byte) param.args[0] != (byte) 3) {
                    return;
                }
                onOnlineSelectOpponentScreenActive();
            }
        });

        /*
        This disables playing against an AI player after 10-15 seconds of not finding an opponent.
        You'll wait longer but you are guaranteed to play human players.
         */
        XposedHelpers.findAndHookMethod("it.megasoft78.trispad.android.AndroidLauncher", lpparam.classLoader, "showWaitingRoomCustom", "com.google.android.gms.games.multiplayer.realtime.Room", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                XposedHelpers.callMethod(mainScreenGameContext, "showAutomatchWaitModal");

//                XposedHelpers.setBooleanField(mainScreenGameContext, "isFakePlayer", true);
//                XposedHelpers.setIntField(mainScreenGameContext, "fakePlayerLevel", 0);
//                XposedHelpers.setIntField(mainScreenGameContext, "opponentXP", 155);
//                XposedHelpers.callMethod(param.thisObject, "leaveRoom", false, false);
//                XposedHelpers.setBooleanField(mainScreenGameContext, "amIPlayerX", true);
//                Object blockedCells = XposedHelpers.callStaticMethod(XposedHelpers.findClass("it.megasoft78.trispad.GameEngine", classLoader), "generateBlockedCells", 3);
//                XposedHelpers.setObjectField(mainScreenGameContext, "blockedCells", blockedCells);
//                XposedHelpers.callMethod(mainScreenGameContext, "enableDisableScreen", true);
//                XposedHelpers.callMethod(param.thisObject, "loadSnapshot", (byte) 1);

                return null;
            }
        });

        XposedHelpers.findAndHookMethod("it.megasoft78.trispad.screens.BoardSizeScreen", lpparam.classLoader, "resize", int.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                onBoardSizeScreenActive();
            }
        });

        XposedHelpers.findAndHookMethod("it.megasoft78.trispad.screens.MainScreen", lpparam.classLoader, "resume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                onMainScreenActive(param);
            }
        });

        XposedHelpers.findAndHookConstructor("it.megasoft78.trispad.screens.MainScreen", lpparam.classLoader, "it.megasoft78.trispad.TrisPadGame", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mainScreenGameContext = param.args[0];
                onMainScreenActive(param);
            }
        });

        XposedHelpers.findAndHookConstructor("it.megasoft78.trispad.TrisPadGame", lpparam.classLoader, "it.megasoft78.trispad.interfaces.IActionResolver", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                currentActionResolver = param.args[0];
            }
        });

        XposedHelpers.findAndHookMethod("it.megasoft78.trispad.TrisPadGame", lpparam.classLoader, "setGameEngine", "it.megasoft78.trispad.GameEngine", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                currentGameEngine = param.args[0];
            }
        });

        XposedHelpers.findAndHookMethod("it.megasoft78.trispad.TrisPadGame", lpparam.classLoader, "getGameEngine", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                currentGameEngine = param.getResult();
            }
        });

        XposedHelpers.findAndHookMethod("it.megasoft78.trispad.screens.GameScreen", lpparam.classLoader, "enableDisableScreen", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                onGameScreenEnableDisableScreen(param);
            }
        });
    }

    private void onMainScreenActive(final XC_MethodHook.MethodHookParam param) {
        delay(new Runnable() {
            @Override
            public void run() {
                XposedHelpers.callMethod(mainScreenGameContext, "getGameEngine");
                XposedHelpers.callMethod(currentGameEngine, "setGameType", 2);
                XposedHelpers.callMethod(currentActionResolver, "loadAndShowOptionsPlayOnlineScreen");
            }
        }, 1000);
    }

    private void onOnlineSelectOpponentScreenActive() {
        delay(new Runnable() {
            @Override
            public void run() {
                XposedHelpers.setBooleanField(mainScreenGameContext, "isRandomOpponent", true);
                XposedHelpers.callMethod(mainScreenGameContext, "openBoardSizeScreen");

            }
        }, 1000);
    }

    private void onBoardSizeScreenActive() {
        delay(new Runnable() {
            @Override
            public void run() {
                XposedHelpers.setIntField(mainScreenGameContext, "boardSize", 3);
                XposedHelpers.callMethod(currentActionResolver, "startNewMatch");
            }
        }, 1000);
    }

    private double evalBoard(byte[][] board, int depth, byte me) {
        if(isWinning(board, me)) {
            return 10.0 - depth;
        }
        if(isWinning(board, me == (byte) 1 ? (byte) 2 : (byte) 1)) {
            return depth - 10.0;
        }
        return 0.0;
    }

    private boolean isDraw(byte[][] board) {
        for(int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (board[y][x] == (byte) 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private int movesLeft(byte[][] board) {
        int movesLeft = 0;
        for(int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (board[y][x] == (byte) 0) {
                    movesLeft++;
                }
            }
        }
        return movesLeft;
    }

    private boolean isBoardEmpty(byte[][] board) {
        for(int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (board[y][x] != (byte) 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private byte[][] makeLastMove(byte[][] board, byte forPlayer) {
        for(int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (board[y][x] == (byte) 0) {
                    board[y][x] = forPlayer;
                    return board;
                }
            }
        }
        return board;
    }

    private boolean isWinning(byte[][] board, byte player) {
        return board[0][0] == player && board[0][1] == player && board[0][2] == player ||
                board[1][0] == player && board[1][1] == player && board[1][2] == player ||
                board[2][0] == player && board[2][1] == player && board[2][2] == player ||

                board[0][0] == player && board[1][0] == player && board[2][0] == player ||
                board[0][1] == player && board[1][1] == player && board[2][1] == player ||
                board[0][2] == player && board[1][2] == player && board[2][2] == player ||

                board[0][0] == player && board[1][1] == player && board[2][2] == player ||
                board[2][0] == player && board[1][1] == player && board[0][2] == player;
    }

    /*
        function minimax(node, depth, maximizingPlayer) is
        if depth = 0 or node is a terminal node then
            return the heuristic value of node
        if maximizingPlayer then
            value := −∞
            for each child of node do
                value := max(value, minimax(child, depth − 1, FALSE))
            return value
        else (* minimizing player *)
            value := +∞
            for each child of node do
                value := min(value, minimax(child, depth − 1, TRUE))
            return value
     */

    private double[] minimax(byte[][] board, int depth, byte player, byte me) {
        if(isDraw(board) || isWinning(board, (byte) 1) || isWinning(board, (byte) 2)) {
            return new double[]{-1, -1, evalBoard(board, depth, me)};
        }

        double[] bestMove;
        if(player == me) {
            bestMove = new double[]{-1, -1, Double.NEGATIVE_INFINITY};
        } else {
            bestMove = new double[]{-1, -1, Double.POSITIVE_INFINITY};
        }

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                if(board[y][x] != (byte) 0) {
                    continue;
                }
                board[y][x] = player;
                double[] score = minimax(board, depth+1, player == (byte) 1 ? (byte) 2 : (byte) 1, me);
                board[y][x] = 0;
                score[0] = x;
                score[1] = y;
                if(player == me) {
                    if(score[2] > bestMove[2]) {
                        bestMove = score;
                    }
                } else {
                    if(score[2] < bestMove[2]) {
                        bestMove = score;
                    }
                }
            }
        }

        return bestMove;
    }

    private void onGameScreenEnableDisableScreen(final XC_MethodHook.MethodHookParam param) {
        boolean enabled = (boolean) param.args[0];
        id++;

        final long me = id;

        if(enabled) {
            delay(new Runnable() {
                @Override
                public void run() {

                    byte[][] board = (byte[][]) XposedHelpers.getObjectField(currentGameEngine, "piecesState");
                    boolean amIPlayerX = XposedHelpers.getBooleanField(mainScreenGameContext, "amIPlayerX");
                    byte player = amIPlayerX ? (byte) 1 : (byte) 2;

                    boolean isPlayer1Playing = (boolean) XposedHelpers.callMethod(currentGameEngine, "isPlayer1Playing");

                    boolean isMyTurn = (amIPlayerX && isPlayer1Playing) || (!amIPlayerX && !isPlayer1Playing);

                    if(id != me)  {
                        sendLocalNotification("Its not my turn, id does not match. Exiting.", context);
                        return;
                    }

                    if(!isMyTurn) {
                        sendLocalNotification("Its not my turn. Exiting.", context);
                        return;
                    }

                    if(movesLeft(board) == 1) {
                        byte[][] newBoard = makeLastMove(board, player);
                        if(isDraw(newBoard)) {
                            sendLocalNotification("Last move is a draw. Waiting for the auto restart", context);
                            return;
                        }
                    }

                    double[] move;
                    if(isBoardEmpty(board)) {
                        int x = (int) Math.floor(Math.random() * 4);
                        int y = (int) Math.floor(Math.random() * 4);
                        move = new double[]{x, y, 10};
                    } else {
                        move = minimax(board, 0, player, player);
                    }

                    Object stage = XposedHelpers.getObjectField(param.thisObject, "stage");
                    Object root = XposedHelpers.callMethod(stage, "getRoot");
                    Object actor = XposedHelpers.callMethod(root, "findActor", "" + String.valueOf((int) move[1]) + "_" + String.valueOf((int) move[0]) + "");

                    if(actor == null) {
                        sendLocalNotification("Actor is null. Exiting.", context);
                        return;
                    }

                    Class<?> inputEventClass = XposedHelpers.findClass("com.badlogic.gdx.scenes.scene2d.InputEvent", classLoader);
                    Object inputEventInstance = XposedHelpers.newInstance(inputEventClass);
                    XposedHelpers.callMethod(inputEventInstance, "reset");
                    XposedHelpers.callMethod(inputEventInstance, "setListenerActor", actor);

                    Object listeners = XposedHelpers.callMethod(actor, "getListeners");
                    Object listener0 = XposedHelpers.callMethod(listeners, "get", 0);
                    XposedHelpers.callMethod(listener0, "clicked", inputEventInstance, 0, 0);
                }
            }, (int) (Math.random() * 700 + 1200));
        }
    }
}
