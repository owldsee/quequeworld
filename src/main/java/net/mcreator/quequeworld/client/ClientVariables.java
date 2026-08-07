package net.mcreator.quequeworld.client;

public class ClientVariables {
	public static boolean hasShield = false;
	public static boolean isThreatened = false;
	public static boolean isGhost = false;


	public static boolean countdownActive = false;
	public static int countdownTicks = 0;
	public static boolean dayTimerActive = false;
	public static boolean dayTimerPaused = false;
	public static int dayTimerTicks = 0;

	/** Timestamp (ms) when the threatened flash animation started. -1 = not playing. */
	public static long threatAnimStart = -1L;

	// Banderines y deudas sincronizados
	public static int banderines = 0;
	public static int deuda = 0;
	public static double dangerLevel = 0.50D;
	public static int minBanderines = 0;

	// Variables interpoladas para animación suave en HUD
	public static double displayedBanderines = 0.0D;
	public static double displayedDeuda = 0.0D;
	public static double displayedDangerLevel = 0.50D;

	// Bandera para inicializar variables sin animación al entrar al juego
	public static boolean firstSync = true;

	// Sistema de bienvenida
	public static String welcomeType = "";       // "img" o "texto"
	public static String welcomeContent = "";    // nombre del banner o texto
	public static long welcomeStartTime = 0L;    // ms cuando empezó la bienvenida
}
