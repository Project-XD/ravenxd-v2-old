package keystrokesmod.module.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import keystrokesmod.Client;
import keystrokesmod.anticrack.AntiCrack;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.ReflectionUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.util.Timer;

import java.lang.reflect.Field;
import java.util.Random;

@SuppressWarnings("DuplicatedCode")
public class Test extends Module {
    public Test() {
        super("Test", category.client);
        this.registerSetting(new ButtonSetting("Test crash", AntiCrack::UNREACHABLE));
    }

    @Override
    public void onEnable() throws Throwable {
        if (!Utils.nullCheck()) {
            disable();
            return;
        }
        Utils.sendMessage("Checking... Please wait.");
        Client.getExecutor().execute(() -> {
            try {
                checkAndReport();
            } catch (Throwable e) {
                Utils.sendMessage(ChatFormatting.RED + "Failed to check compatibility!");
                Utils.handleException(e, "check compatibility");
            }
        });
        disable();
    }

    private static void checkAndReport() throws Throwable {
        StringBuilder result = new StringBuilder();
        result.append(ChatFormatting.BOLD);
        result.append("---Compatibility Report---\n");
        result.append(ChatFormatting.RESET);

        result.append(ChatFormatting.GRAY);
        result.append("Reflection:\n");
        result.append(ChatFormatting.RESET);

        result.append("    Minecraft    ");
        try {
            Object fieldTimer = ReflectionUtils.get(mc, "field_71428_T");
            Timer timer = Utils.getTimer();
            if (fieldTimer == timer) {
                result.append(ChatFormatting.GREEN);
                result.append("OK");
            } else {
                result.append(ChatFormatting.RED);
                result.append("\n        ");
                result.append(String.format("Wrong result: should be '%s' at '%s', but got '%s' at %s",
                        timer.getClass(),
                        System.identityHashCode(timer),
                        fieldTimer.getClass(),
                        System.identityHashCode(fieldTimer)
                ));
            }
        } catch (Throwable e) {
            result.append(ChatFormatting.RED);
            result.append("\n        ");
            result.append(String.format("Unexpected '%s': %s",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            ));
        }
        result.append(ChatFormatting.RESET);
        result.append("\n");

        result.append("    Self    ");
        try {
            Object fieldName = ReflectionUtils.getDeclared(Client.class, "NAME");
            String name = Client.NAME;
            if (fieldName == name) {
                result.append(ChatFormatting.GREEN);
                result.append("OK");
            } else {
                result.append(ChatFormatting.RED);
                result.append("\n        ");
                result.append(String.format("Wrong result: should be '%s' at '%s', but got '%s' at %s",
                        name.getClass(),
                        System.identityHashCode(name),
                        fieldName.getClass(),
                        System.identityHashCode(fieldName)
                ));
            }
        } catch (Throwable e) {
            result.append(ChatFormatting.RED);
            result.append("\n        ");
            result.append(String.format("Unexpected '%s': %s",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            ));
        }
        result.append(ChatFormatting.RESET);
        result.append("\n");

        result.append("    Self2    ");
        try {
            Object fieldName = ReflectionUtils.getDeclared(AntiCrack.class, "CLIENT_NAME");
            String name = AntiCrack.CLIENT_NAME;
            if (fieldName == name) {
                result.append(ChatFormatting.GREEN);
                result.append("OK");
            } else {
                result.append(ChatFormatting.RED);
                result.append("\n        ");
                result.append(String.format("Wrong result: should be '%s' at '%s', but got '%s' at %s",
                        name.getClass(),
                        System.identityHashCode(name),
                        fieldName.getClass(),
                        System.identityHashCode(fieldName)
                ));
            }
        } catch (Throwable e) {
            result.append(ChatFormatting.RED);
            result.append("\n        ");
            result.append(String.format("Unexpected '%s': %s",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            ));
        }
        result.append(ChatFormatting.RESET);
        result.append("\n");

        result.append(ChatFormatting.GRAY);
        result.append("Benchmarking:\n");
        result.append(ChatFormatting.RESET);

        result.append("    Normal:    ");
        result.append(ChatFormatting.GREEN);
        result.append(String.format("%.4f", timeIt(Test::benchmark)));
        result.append(ChatFormatting.RESET);
        result.append("\n");
        result.append("    Native:    ");
        result.append(ChatFormatting.GREEN);
        result.append(String.format("%.4f", timeIt(Test::benchmarkNative)));
        result.append(ChatFormatting.RESET);
        result.append("\n");

        Utils.sendMessage(result.toString());
    }

    private static double timeIt(Runnable runnable) {
        long startTime = System.nanoTime();
        for (int i = 0; i < 2; i++) {
            runnable.run();
        }
        long stopTime = System.nanoTime();
        return (double) (stopTime - startTime) / 5 / 1.0000E+9;
    }

    private static void benchmark() {
        long result = 1;

        // 1. Runtime evaluation
        for (int i = 0; i < result; i++) {
            if (i < 1000 || result > System.currentTimeMillis()) {
                result += i;
            }
        }

        // 2. System call
        for (int i = 0; i < 1000; i++) {
            result = Math.max(System.nanoTime(), result);
        }

        // 3. Reflection
        result = Math.min(result, 0);
        for (int i = 0; i < 100; i++) {
            try {
                Field fieldTimer = mc.getClass().getDeclaredField("field_71428_T");
                fieldTimer.setAccessible(true);
                result += fieldTimer.get(mc).getClass() == Timer.class ? 1 : 0;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        // 4. Matrix multiplication
        int size = 50; // Matrix size
        Random random = new Random();
        double[][] matrix1 = new double[size][size];
        for (int i2 = 0; i2 < size; i2++) {
            for (int j2 = 0; j2 < size; j2++) {
                matrix1[i2][j2] = random.nextDouble();
            }
        }
        double[][] matrix = new double[size][size];
        for (int i1 = 0; i1 < size; i1++) {
            for (int j1 = 0; j1 < size; j1++) {
                matrix[i1][j1] = random.nextDouble();
            }
        }
        int size1 = matrix1.length;
        double[][] result1 = new double[size1][size1];
        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size1; j++) {
                for (int k = 0; k < size1; k++) {
                    result1[i][j] += matrix1[i][k] * matrix[k][j];
                }
            }
        }

        // Use the first element of the result to prevent optimization
        result += (long) result1[0][0];

        // 5. Diffusion (recursive computation)
        int depth = 20; // Depth of recursion
        result += diffusion(depth);

        // Prevent result from being optimized away
        // Simple no-op method to prevent result from being optimized away
        if (result == 0) {
            System.out.print(""); // Ensure the result is "used"
        }
    }

    private static void benchmarkNative() {
        long result = 1;

        // 1. Runtime evaluation
        for (int i = 0; i < result; i++) {
            if (i < 10000 || result > System.currentTimeMillis()) {
                result += i;
            }
        }

        // 2. System call
        for (int i = 0; i < 10000; i++) {
            result = Math.max(System.nanoTime(), result);
        }

        // 3. Reflection
        result = Math.min(result, 0);
        for (int i = 0; i < 10000; i++) {
            try {
                Field fieldTimer = mc.getClass().getDeclaredField("field_71428_T");
                fieldTimer.setAccessible(true);
                result += fieldTimer.get(mc).getClass() == Timer.class ? 1 : 0;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        // 4. Matrix multiplication
        int size = 100; // Matrix size
        Random random = new Random();
        double[][] matrix1 = new double[size][size];
        for (int i2 = 0; i2 < size; i2++) {
            for (int j2 = 0; j2 < size; j2++) {
                matrix1[i2][j2] = random.nextDouble();
            }
        }
        double[][] matrix = new double[size][size];
        for (int i1 = 0; i1 < size; i1++) {
            for (int j1 = 0; j1 < size; j1++) {
                matrix[i1][j1] = random.nextDouble();
            }
        }
        int size1 = matrix1.length;
        double[][] result1 = new double[size1][size1];
        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size1; j++) {
                for (int k = 0; k < size1; k++) {
                    result1[i][j] += matrix1[i][k] * matrix[k][j];
                }
            }
        }

        // Use the first element of the result to prevent optimization
        result += (long) result1[0][0];

        // 5. Diffusion (recursive computation)
        int depth = 25; // Depth of recursion
        result += diffusionNative(depth);

        // Prevent result from being optimized away
        // Simple no-op method to prevent result from being optimized away
        if (result == 0) {
            System.out.print(""); // Ensure the result is "used"
        }
    }

    private static long diffusion(int depth) {
        if (depth == 0) {
            return 1;
        }
        return diffusion(depth - 1) + diffusion(depth - 1);
    }

    private static long diffusionNative(int depth) {
        if (depth == 0) {
            return 1;
        }
        return diffusionNative(depth - 1) + diffusionNative(depth - 1);
    }

}
