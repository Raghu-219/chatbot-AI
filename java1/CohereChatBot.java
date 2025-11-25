import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.json.*;
import java.awt.datatransfer.StringSelection;

public class CohereChatBot extends JFrame {
    private static final String COHERE_API_KEY = "SZoFo4rhvY2r7m3zwmhxTCIWdMyzwV071xZVofBp";
    private static final String COHERE_API_URL = "https://api.cohere.ai/v1/chat";
    private static final String COHERE_MODEL = "command-xlarge-nightly";

    private final JPanel messageList = new JPanel();
    private final JScrollPane scrollPane = new JScrollPane(messageList);
    private final PlaceholderTextField inputField = new PlaceholderTextField("Type a message...");
    private final JButton sendButton = new JButton("\u27A4");
    private final JLabel typingLabel = new JLabel("");

    public CohereChatBot() {
        super("UNIVERSAL-chatbot ");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        initUI();
        addMessage("Bot", "Hello! I'm your assistant. Ask me anything.", false);
    }

    private void initUI() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(new Color(18, 24, 38));
        topBar.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel title = new JLabel("UNIVERSAL-Chatbot");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Inter", Font.BOLD, 18));
        topBar.add(title, BorderLayout.WEST);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        JButton settings = iconButton("\u2699");
        JButton clear = iconButton("\u267B");
        settings.setToolTipText("Settings");
        clear.setToolTipText("Clear chat");
        clear.addActionListener(e -> clearMessages());
        topRight.add(clear);
        topRight.add(settings);
        topBar.add(topRight, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        messageList.setLayout(new BoxLayout(messageList, BoxLayout.Y_AXIS));
        messageList.setBackground(new Color(248, 250, 252));
        messageList.setBorder(new EmptyBorder(8, 8, 8, 8));

        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(248, 250, 252));
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputBar = new JPanel(new BorderLayout(8, 8));
        inputBar.setBorder(new EmptyBorder(12, 12, 12, 12));
        inputBar.setBackground(new Color(248, 250, 252));

        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        inputField.setPreferredSize(new Dimension(0, 36));
        inputField.addActionListener(e -> sendMessage());

        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(0, 120, 212));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(new EmptyBorder(8, 12, 8, 12));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> sendMessage());

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setOpaque(false);
        left.add(inputField, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(sendButton, BorderLayout.CENTER);

        inputBar.add(left, BorderLayout.CENTER);
        inputBar.add(right, BorderLayout.EAST);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        typingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        typingLabel.setForeground(new Color(100, 100, 120));
        bottomRow.add(typingLabel, BorderLayout.WEST);
        bottomRow.add(inputBar, BorderLayout.CENTER);

        add(bottomRow, BorderLayout.SOUTH);

        messageList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { maybeShowMenu(e); }
            public void mouseReleased(MouseEvent e) { maybeShowMenu(e); }
            private void maybeShowMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem copyAll = new JMenuItem("Copy all messages");
                    copyAll.addActionListener(a -> copyAllMessages());
                    menu.add(copyAll);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private JButton iconButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        b.setBackground(new Color(27, 34, 45));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 10, 8, 10));
        return b;
    }

    private void addMessage(String sender, String message, boolean isUser) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(6, 6, 6, 6));

        Avatar avatar = new Avatar(isUser ? "Y" : "C", isUser ? new Color(0, 120, 212) : new Color(70, 80, 90));
        JPanel avatarWrap = new JPanel(new BorderLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.add(avatar, BorderLayout.NORTH);

        BubblePanel bubble = new BubblePanel(message, isUser);
        JPanel bubbleWrap = new JPanel(new BorderLayout());
        bubbleWrap.setOpaque(false);
        if (isUser) {
            bubbleWrap.add(bubble, BorderLayout.EAST);
            row.add(bubbleWrap, BorderLayout.CENTER);
            row.add(avatarWrap, BorderLayout.EAST);
        } else {
            bubbleWrap.add(bubble, BorderLayout.WEST);
            row.add(avatarWrap, BorderLayout.WEST);
            row.add(bubbleWrap, BorderLayout.CENTER);
        }

        messageList.add(row);
        messageList.revalidate();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar()
                .setValue(scrollPane.getVerticalScrollBar().getMaximum()));
    }

    private void clearMessages() {
        messageList.removeAll();
        messageList.revalidate();
        messageList.repaint();
    }

    private void copyAllMessages() {
        StringBuilder sb = new StringBuilder();
        for (Component comp : messageList.getComponents()) {
            if (comp instanceof JPanel) {}
        }
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(sb.toString()), null);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        addMessage("You", text, true);
        inputField.setText("");
        setTyping(true);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try { Thread.sleep(350); } catch (InterruptedException ignored) {}
                return getCohereResponse(text);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    addMessage("Bot", response, false);
                } catch (Exception e) {
                    addMessage("Bot", "Error: " + e.getMessage(), false);
                } finally {
                    setTyping(false);
                }
            }
        };
        worker.execute();
    }

    private void setTyping(boolean on) {
        if (on) startTypingAnimation();
        else {
            stopTypingAnimation();
            typingLabel.setText("");
        }
    }

    private Timer typingTimer;
    private int dotCount = 0;

    private void startTypingAnimation() {
        typingLabel.setText("Cohere is typing");
        dotCount = 0;
        typingTimer = new Timer(400, e -> {
            dotCount = (dotCount + 1) % 4;
            String dots = ".".repeat(dotCount);
            typingLabel.setText("Cohere is typing" + dots);
        });
        typingTimer.start();
    }

    private void stopTypingAnimation() {
        if (typingTimer != null) {
            typingTimer.stop();
            typingTimer = null;
        }
    }

    private String getCohereResponse(String userMessage) {
        if (COHERE_API_KEY == null || COHERE_API_KEY.isEmpty()) {
            return "API key not set. Set COHERE_API_KEY environment variable.";
        }
        try {
            URL url = new URL(COHERE_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + COHERE_API_KEY);
            conn.setDoOutput(true);

            JSONObject requestBody = new JSONObject();
            requestBody.put("message", userMessage);
            requestBody.put("model", COHERE_MODEL);
            requestBody.put("temperature", 0.3);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            InputStream stream = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder resp = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) resp.append(line);
                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject json = new JSONObject(resp.toString());
                    if (json.has("text")) return json.getString("text");
                    return json.toString();
                } else {
                    return "API Error (" + responseCode + "): " + resp.toString();
                }
            }
        } catch (Exception e) {
            return "Connection error: " + e.getMessage();
        }
    }

    static class PlaceholderTextField extends JTextField {
        private String placeholder;
        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setColumns(1);
            setBorder(null);
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { repaint(); }
                public void focusLost(FocusEvent e) { repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                g2.setColor(new Color(140, 145, 150));
                Insets ins = getInsets();
                g2.drawString(placeholder, ins.left + 4, getHeight() / 2 + g2.getFontMetrics().getAscent()/2 - 2);
                g2.dispose();
            }
        }
    }

    static class Avatar extends JComponent {
        private final String text;
        private final Color color;
        public Avatar(String text, Color color) {
            this.text = text;
            this.color = color;
            setPreferredSize(new Dimension(32, 32));
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0,0,getWidth(),getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(text);
            int th = fm.getAscent();
            g2.drawString(text, (getWidth()-tw)/2, (getHeight()+th)/2 - 2);
            g2.dispose();
        }
    }

    static class BubblePanel extends JPanel {
        private final String text;
        private final boolean isUser;
        private final String time;
        public BubblePanel(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
            this.time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
            setOpaque(false);
            setLayout(new BorderLayout());
            JLabel lbl = new JLabel("<html>" + escapeHtml(text).replace("\n","<br/>") + "</html>");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(isUser ? Color.WHITE : new Color(20, 20, 20));
            lbl.setBorder(new EmptyBorder(6, 8, 4, 8));
            add(lbl, BorderLayout.CENTER);
            JLabel ts = new JLabel(time);
            ts.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            ts.setForeground(isUser ? new Color(230,230,230) : new Color(120,120,130));
            ts.setBorder(new EmptyBorder(0, 8, 6, 8));
            add(ts, BorderLayout.SOUTH);
            setBorder(new EmptyBorder(2,2,2,2));
        }

        @Override
        protected void paintComponent(Graphics g) {
            int arc = 12;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (isUser) {
                g2.setColor(new Color(0, 120, 212));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                int[] xs = {w-6, w+4, w-6};
                int[] ys = {h-14, h-9, h-6};
                g2.fillPolygon(xs, ys, 3);
            } else {
                g2.setColor(new Color(240, 243, 245));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }

        private static String escapeHtml(String s) {
            return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CohereChatBot ui = new CohereChatBot();
            ui.setVisible(true);
        });
    }
}
