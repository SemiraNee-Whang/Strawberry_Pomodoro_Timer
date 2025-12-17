
import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StrawberryPomodoro {

    // 🎨 Colors
    private static final Color STRAWBERRY_PINK = new Color(255, 120, 120);
    private static final Color LIGHT_PINK = new Color(255, 235, 235);
    private static final Color BACKGROUND_GRAY = new Color(245, 245, 245);
    private static final Color TEXT_DARK = new Color(60, 60, 60);
    private static final Color BUTTON_GRAY = new Color(230, 230, 230);
    private static final Color SUBJECT_BG = new Color(255, 210, 210);
    private static final Color SUBJECT_SELECTED_BG = STRAWBERRY_PINK;

    // ⏱️ Times
    private static final int POMODORO_MIN = 25;
    private static final int SHORT_BREAK_MIN = 5;
    private static final int LONG_BREAK_MIN = 15;

    // Logic
    private Timer timer;
    private Timer dailyCheckTimer;
    private int totalSeconds;
    private boolean isRunning = false;
    private String currentMode = "Pomodoro";
    private String currentSubject = null;
    private int pomodoroCount = 0; // track completed Pomodoros for long break

    // Progress
    private int completedSessions = 0;
    private int totalStudyMinutes = 0;
    private LocalDate savedDate = LocalDate.now();

    // Subjects
    private Map<String, SubjectProgress> subjects = new HashMap<>();
    private JPanel subjectsPanel;

    // UI
    private JLabel timeLabel;
    private JLabel statusLabel;
    private JLabel sessionsLabel;
    private JLabel timeProgressLabel;
    private JButton startButton;
    private JButton pomodoroButton;
    private JButton shortBreakButton;
    private JButton longBreakButton;
    private JButton addSubjectButton;

    // Music
    private Clip musicClip;
    private Clip alarmClip;
    private String[] breakMusicPaths = {
        "D:\\Documents\\Music\\[no copyright music] 2_00 AM cute background music [rKi3oL2UDew].wav",
        "D:\\Documents\\Music\\[no copyright music] '2_00 AM' cute background music [rKi3oL2UDew].wav",
        "D:\\Documents\\Music\\[no copyright music] Dreamy Mode cute background music [hCtwi8XkB4o].wav",
        "D:\\Documents\\Music\\[no copyright music] Gameplay cute background music [w4oLP7fa9Vk].wav",
        "D:\\Documents\\Music\\[no copyright music] In Dreamland background music [DSWYAclv2I8].wav",
        "D:\\Documents\\Music\\[no copyright music] Taiyaki cute background music [zde7oFYW4Zg].wav",
        "D:\\Documents\\Music\\[no copyright music] Purple lofi background music [BWNx0VQJjMY].wav"
    };
    private final String alarmPath = "D:\\Documents\\Music\\iphone_alarm.wav";

    public StrawberryPomodoro() {
        JFrame frame = new JFrame("🍓 Strawberry Study Timer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BACKGROUND_GRAY);

        // ===== HEADER =====
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(LIGHT_PINK);
        header.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        JLabel title = new JLabel("🍓 Strawberry Study Timer");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Stay focused and track your study sessions");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        frame.add(header, BorderLayout.NORTH);

        // ===== MAIN CONTENT =====
        JPanel mainContent = new JPanel(new BorderLayout());
        frame.add(mainContent, BorderLayout.CENTER);

        // ===== LEFT PANEL (TIMER & CONTROL) =====
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Timer panel
        JPanel timerPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 260;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.setStroke(new BasicStroke(16));
                g2.setColor(new Color(255, 190, 190));
                g2.drawOval(x, y, size, size);
            }
        };
        timerPanel.setPreferredSize(new Dimension(500, 340));
        timerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        timerPanel.setBackground(Color.WHITE);

        JPanel timerText = new JPanel();
        timerText.setLayout(new BoxLayout(timerText, BoxLayout.Y_AXIS));
        timerText.setOpaque(false);

        timeLabel = new JLabel("25:00");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 64));
        timeLabel.setForeground(STRAWBERRY_PINK);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("Focus Session");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        statusLabel.setForeground(new Color(255, 140, 140));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timerText.add(timeLabel);
        timerText.add(Box.createVerticalStrut(8));
        timerText.add(statusLabel);
        timerPanel.add(timerText);
        leftPanel.add(timerPanel);
        leftPanel.add(Box.createVerticalStrut(30));

        // Mode buttons
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        modePanel.setBackground(Color.WHITE);

        pomodoroButton = createModeButton("Pomodoro");
        shortBreakButton = createModeButton("Short Break");
        longBreakButton = createModeButton("Long Break");

        modePanel.add(pomodoroButton);
        modePanel.add(shortBreakButton);
        modePanel.add(longBreakButton);

        leftPanel.add(modePanel);
        leftPanel.add(Box.createVerticalStrut(25));

        // Control buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        controlPanel.setBackground(Color.WHITE);

        startButton = createActionButton("▶ Start", STRAWBERRY_PINK);
        JButton resetButton = createActionButton("↺ Reset", BUTTON_GRAY);
        resetButton.setForeground(TEXT_DARK);

        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        leftPanel.add(controlPanel);
        leftPanel.add(Box.createVerticalStrut(40));

        // Daily progress
        JLabel progressTitle = new JLabel("Today's Progress");
        progressTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        progressTitle.setForeground(STRAWBERRY_PINK);
        progressTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        sessionsLabel = new JLabel("Completed Sessions: 0");
        sessionsLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        sessionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timeProgressLabel = new JLabel("Total Study Time: 0 min");
        timeProgressLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        timeProgressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(progressTitle);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(sessionsLabel);
        leftPanel.add(timeProgressLabel);

        mainContent.add(leftPanel, BorderLayout.CENTER);

        // ===== RIGHT PANEL (SUBJECTS) =====
        subjectsPanel = new JPanel();
        subjectsPanel.setLayout(new BoxLayout(subjectsPanel, BoxLayout.Y_AXIS));
        subjectsPanel.setBackground(LIGHT_PINK);
        subjectsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addSubjectButton = new JButton("+ Add Subject");
        addSubjectButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        addSubjectButton.setFocusPainted(false);
        addSubjectButton.setBackground(Color.WHITE);
        addSubjectButton.setBorder(BorderFactory.createLineBorder(new Color(255, 160, 160), 2, true));
        addSubjectButton.setHorizontalAlignment(SwingConstants.LEFT);
        addSubjectButton.setMaximumSize(new Dimension(350, 35));
        addSubjectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addSubjectButton.addActionListener(e -> addSubject());

        subjectsPanel.add(addSubjectButton);
        subjectsPanel.add(Box.createVerticalStrut(10));

        JScrollPane scrollPane = new JScrollPane(subjectsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(380, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainContent.add(scrollPane, BorderLayout.EAST);

        // ===== TIMER LOGIC =====
        timer = new Timer(1000, e -> {
            if (totalSeconds > 0) {
                totalSeconds--;
                timeLabel.setText(formatTime(totalSeconds));
            } else {
                timer.stop();
                isRunning = false;
                startButton.setText("▶ Start");
                playAlarm();

                if (currentMode.equals("Pomodoro")) {
                    pomodoroCount++;
                    completedSessions++;
                    totalStudyMinutes += POMODORO_MIN;
                    if (currentSubject != null) {
                        SubjectProgress sp = subjects.get(currentSubject);
                        if (sp != null) {
                            sp.sessions++;
                            sp.minutes += POMODORO_MIN;
                        }
                    }
                    sessionsLabel.setText("Completed Sessions: " + completedSessions);
                    timeProgressLabel.setText("Total Study Time: " + totalStudyMinutes + " min");
                    saveProgress();

                    // Auto switch to break
                    if (pomodoroCount % 4 == 0) setMode("Long Break");
                    else setMode("Short Break");
                    toggleTimer();
                } else {
                    // Break ended → start next Pomodoro automatically
                    setMode("Pomodoro");
                    toggleTimer();
                }
            }
        });

        // ===== AUTO RESET AT MIDNIGHT =====
        dailyCheckTimer = new Timer(1000 * 60, e -> {
            if (!savedDate.equals(LocalDate.now())) {
                savedDate = LocalDate.now();
                completedSessions = 0;
                totalStudyMinutes = 0;
                sessionsLabel.setText("Completed Sessions: 0");
                timeProgressLabel.setText("Total Study Time: 0 min");
                for (SubjectProgress sp : subjects.values()) {
                    sp.sessions = 0;
                    sp.minutes = 0;
                }
                updateSubjectsPanel();
                saveProgress();
            }
        });
        dailyCheckTimer.start();

        // ===== BUTTON ACTIONS =====
        pomodoroButton.addActionListener(e -> setMode("Pomodoro"));
        shortBreakButton.addActionListener(e -> setMode("Short Break"));
        longBreakButton.addActionListener(e -> setMode("Long Break"));
        startButton.addActionListener(e -> toggleTimer());
        resetButton.addActionListener(e -> resetTimer());

        setMode("Pomodoro");
        loadProgress();
        frame.setVisible(true);
    }

    // ===== SUBJECT LOGIC =====
    private void addSubject() {
        String subject = JOptionPane.showInputDialog("Enter subject name:");
        if (subject != null && !subject.trim().isEmpty() && !subjects.containsKey(subject)) {
            subjects.put(subject, new SubjectProgress());
            currentSubject = subject;
            updateSubjectsPanel();
        }
    }

    private void updateSubjectsPanel() {
        subjectsPanel.removeAll();
        subjectsPanel.add(addSubjectButton);
        subjectsPanel.add(Box.createVerticalStrut(10));

        for (String name : subjects.keySet()) {
            SubjectProgress sp = subjects.get(name);

            JTextArea label = new JTextArea(name + ": " + sp.sessions + " sessions, " + sp.minutes + " min");
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            label.setForeground(TEXT_DARK);
            label.setEditable(false);
            label.setLineWrap(true);
            label.setWrapStyleWord(true);
            label.setOpaque(false);

            JButton selectButton = new JButton("Select");
            selectButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
            selectButton.setBackground(BUTTON_GRAY);
            selectButton.setForeground(TEXT_DARK);
            selectButton.setFocusPainted(false);
            selectButton.addActionListener(e -> {
                currentSubject = name;
                updateSubjectsPanel();
                label.scrollRectToVisible(label.getBounds());
            });

            JButton removeButton = new JButton("Remove");
            removeButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
            removeButton.setBackground(new Color(255, 100, 100));
            removeButton.setForeground(Color.WHITE);
            removeButton.setFocusPainted(false);
            removeButton.addActionListener(e -> {
                subjects.remove(name);
                if (currentSubject != null && currentSubject.equals(name)) currentSubject = null;
                updateSubjectsPanel();
                saveProgress();
            });

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(name.equals(currentSubject) ? SUBJECT_SELECTED_BG : SUBJECT_BG);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            topRow.setOpaque(false);
            topRow.add(label);
            topRow.add(selectButton);
            topRow.add(removeButton);

            panel.add(topRow);
            panel.setMaximumSize(new Dimension(350, 80));

            subjectsPanel.add(panel);
            subjectsPanel.add(Box.createVerticalStrut(10));
        }

        subjectsPanel.revalidate();
        subjectsPanel.repaint();
    }

    static class SubjectProgress {
        int sessions = 0;
        int minutes = 0;
    }

    // ===== MUSIC & ALARM =====
    private void playMusicRandom() {
        stopMusic();
        if (breakMusicPaths.length == 0) return;

        int index = (int) (Math.random() * breakMusicPaths.length);
        String path = breakMusicPaths[index];

        try {
            File audioFile = new File(path);
            if (!audioFile.exists()) return;

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioStream);
            musicClip.start();
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
            musicClip.close();
        }
    }

    private void playAlarm() {
        try {
            File file = new File(alarmPath);
            if (!file.exists()) return;

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            alarmClip = AudioSystem.getClip();
            alarmClip.open(stream);
            alarmClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== HELPERS =====
    private JButton createModeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(BUTTON_GRAY);
        btn.setForeground(TEXT_DARK);
        btn.setPreferredSize(new Dimension(130, 38));
        return btn;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(170, 45));
        return btn;
    }

    private void setMode(String mode) {
        timer.stop();
        isRunning = false;
        startButton.setText("▶ Start");

        pomodoroButton.setBackground(BUTTON_GRAY);
        shortBreakButton.setBackground(BUTTON_GRAY);
        longBreakButton.setBackground(BUTTON_GRAY);

        int minutes = POMODORO_MIN;
        statusLabel.setText("Focus Session");
        stopMusic();

        if (mode.equals("Short Break")) {
            minutes = SHORT_BREAK_MIN;
            statusLabel.setText("Short Break");
            shortBreakButton.setBackground(STRAWBERRY_PINK);
            playMusicRandom();
        } else if (mode.equals("Long Break")) {
            minutes = LONG_BREAK_MIN;
            statusLabel.setText("Long Break");
            longBreakButton.setBackground(STRAWBERRY_PINK);
            playMusicRandom();
        } else {
            pomodoroButton.setBackground(STRAWBERRY_PINK);
        }

        currentMode = mode;
        totalSeconds = minutes * 60;
        timeLabel.setText(formatTime(totalSeconds));
    }

    private void toggleTimer() {
        if (!isRunning) {
            timer.start();
            startButton.setText("⏸ Pause");
            isRunning = true;
        } else {
            timer.stop();
            startButton.setText("▶ Resume");
            isRunning = false;
        }
    }

    private void resetTimer() {
        timer.stop();
        setMode(currentMode);
    }

    private String formatTime(int sec) {
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    // ===== SAVE/LOAD =====
    private void saveProgress() {
        try (PrintWriter pw = new PrintWriter("strawberry_progress.txt")) {
            pw.println(LocalDate.now());
            pw.println(completedSessions);
            pw.println(totalStudyMinutes);
            pw.println(subjects.size());
            for (String name : subjects.keySet()) {
                SubjectProgress sp = subjects.get(name);
                pw.println(name);
                pw.println(sp.sessions);
                pw.println(sp.minutes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProgress() {
        File file = new File("strawberry_progress.txt");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            savedDate = LocalDate.parse(br.readLine());
            completedSessions = Integer.parseInt(br.readLine());
            totalStudyMinutes = Integer.parseInt(br.readLine());
            int n = Integer.parseInt(br.readLine());
            for (int i = 0; i < n; i++) {
                String name = br.readLine();
                SubjectProgress sp = new SubjectProgress();
                sp.sessions = Integer.parseInt(br.readLine());
                sp.minutes = Integer.parseInt(br.readLine());
                subjects.put(name, sp);
            }
            sessionsLabel.setText("Completed Sessions: " + completedSessions);
            timeProgressLabel.setText("Total Study Time: " + totalStudyMinutes + " min");
            updateSubjectsPanel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StrawberryPomodoro::new);
    }
}
