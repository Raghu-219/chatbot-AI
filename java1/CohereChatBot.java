import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.json.JSONObject;

/**
 * CohereChatBot - Material-style chat UI
 */
public class CohereChatBot extends JFrame {

    private final JPanel messageList = new JPanel();
    private final JScrollPane scrollPane = new JScrollPane(messageList);
    private final PlaceholderTextField inputField =
            new PlaceholderTextField("Type a message...");
    private final JButton sendButton;
    private final JLabel typingLabel = new JLabel(" ");

    private final List<JSONObject> conversationHistory = new ArrayList<>();

    // <<< PUT YOUR REAL KEY HERE >>>
    private static final String COHERE_API_KEY = "JdIlhyujw6r5xsEjAXqvs8HsZTkpII04GbPw8wjy";
    private static final String COHERE_CHAT_URL = "https://api.cohere.ai/v1/chat";
    private static final String CURRENT_MODEL = "command-a-03-2025";

    // Material-ish palette
    private static final Color BG = new Color(248, 244, 240);
    private static final Color CARD = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(98, 0, 238);
    private static final Color ACCENT = new Color(124, 77, 255);
    private static final Color USER_BUBBLE = new Color(225, 242, 255);
    private static final Color BOT_BUBBLE = new Color(250, 250, 255);
    private static final Color SUB_TEXT = new Color(120, 120, 130);

    public CohereChatBot() {
        super("Universal Chatbot - Material UI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        sendButton = new RoundedButton("Send", ACCENT, Color.WHITE);
        sendButton.setFont(new Font("Inter", Font.BOLD, 14));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.setMargin(new Insets(10, 18, 10, 18));
        sendButton.setPreferredSize(new Dimension(88, 40));
        sendButton.addActionListener(e -> sendMessage());

        typingLabel.setFont(new Font("Inter", Font.ITALIC, 13));
        typingLabel.setForeground(SUB_TEXT);
        typingLabel.setHorizontalAlignment(SwingConstants.LEFT);

        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(BG);
        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    // ---------- TOP BAR ----------

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(22, 26, 8, 26));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        left.setOpaque(false);

        JLabel logoIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int size = Math.min(getWidth(), getHeight());
                g2.setColor(ACCENT);
                g2.fillOval(0, 0, size, size);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Inter", Font.BOLD, 18));
                String label = "UB";
                FontMetrics fm = g2.getFontMetrics();
                int tx = (size - fm.stringWidth(label)) / 2;
                int ty = (size + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(label, tx, ty);
                g2.dispose();
            }
        };
        logoIcon.setPreferredSize(new Dimension(52, 52));

        JLabel title = new JLabel(
                "<html><span style='font-family:Inter; font-weight:700; font-size:18pt;'>Universal Chatbot</span>"
                        + "<br><span style='font-family:Inter; font-weight:400; font-size:10pt; color:#777777;'>Hi! How can I assist you today?</span></html>");
        left.add(logoIcon);
        left.add(title);

        topPanel.add(left, BorderLayout.WEST);
        return topPanel;
    }

    // ---------- CENTER (chips + chat) ----------

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(4, 26, 8, 26));

        // quick action chips
        JPanel categoriesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        categoriesPanel.setOpaque(false);
        String[] categories = {
                "Explain Java", "Code review", "Debug help", "System design",
                "AR/VR ideas", "Full-stack tips", "Chatbot tutorial", "Clear chat"
        };
        for (String txt : categories) {
            JButton chip = new JButton(txt);
            chip.setFont(new Font("Inter", Font.PLAIN, 13));
            chip.setBackground(Color.WHITE);
            chip.setBorder(new RoundedBorder(24, new Color(220, 220, 220)));
            chip.setFocusPainted(false);
            chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            chip.setPreferredSize(new Dimension(150, 34));
            chip.addActionListener(e -> {
                if ("Clear chat".equals(txt)) {
                    messageList.removeAll();
                    conversationHistory.clear();
                    messageList.revalidate();
                    messageList.repaint();
                } else {
                    inputField.setText(txt + " ");
                    inputField.requestFocusInWindow();
                }
            });
            categoriesPanel.add(chip);
        }

        // chat area
        messageList.setLayout(new BoxLayout(messageList, BoxLayout.Y_AXIS));
        messageList.setBackground(BG);
        messageList.setOpaque(true);
        messageList.setBorder(new EmptyBorder(12, 12, 12, 12));

        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // material card wrapper for chat
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setOpaque(true);
        card.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(categoriesPanel, BorderLayout.NORTH);
        centerPanel.add(card, BorderLayout.CENTER);
        return centerPanel;
    }

    // ---------- BOTTOM INPUT BAR ----------

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 26, 20, 26));

        // typing label
        JPanel typingRow = new JPanel(new BorderLayout());
        typingRow.setOpaque(false);
        typingRow.add(typingLabel, BorderLayout.WEST);
        bottomPanel.add(typingRow, BorderLayout.NORTH);

        // rounded card with input + send
        JPanel inputCard = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 18;
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.setColor(new Color(0, 0, 0, 15));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        inputCard.setOpaque(false);
        inputCard.setBorder(new EmptyBorder(8, 12, 8, 12));
        inputCard.setPreferredSize(new Dimension(800, 64));

        JLabel iconLabel = new JLabel("\uD83D\uDD0D");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLabel.setForeground(new Color(140, 140, 150));
        iconLabel.setBorder(new EmptyBorder(4, 4, 4, 8));
        inputCard.add(iconLabel, BorderLayout.WEST);

        inputField.setFont(new Font("Inter", Font.PLAIN, 14));
        inputField.setBorder(null);
        inputField.setOpaque(false);
        inputField.addActionListener(e -> sendMessage());
        inputCard.add(inputField, BorderLayout.CENTER);

        JPanel sendWrap = new JPanel(new GridBagLayout());
        sendWrap.setOpaque(false);
        sendWrap.add(sendButton);
        inputCard.add(sendWrap, BorderLayout.EAST);

        bottomPanel.add(inputCard, BorderLayout.CENTER);
        return bottomPanel;
    }

    // ---------- SEND / API ----------

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        addMessageBubble("You", text, USER_BUBBLE, FlowLayout.RIGHT);
        inputField.setText("");
        typingLabel.setText("UB is typing...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try {
                    if (COHERE_API_KEY.equals("YOUR_COHERE_API_KEY")) {
                        return "Set your Cohere API key in COHERE_API_KEY to get live responses.";
                    }
                    return callCohereAPI(text);
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String reply = get();
                    addMessageBubble("Cohere", reply, BOT_BUBBLE, FlowLayout.LEFT);
                } catch (Exception e) {
                    addMessageBubble("Bot", "Failed to get response.", BOT_BUBBLE, FlowLayout.LEFT);
                }
                typingLabel.setText(" ");
            }
        };
        worker.execute();
    }

    private String callCohereAPI(String userMessage) throws Exception {
        JSONObject request = new JSONObject();
        request.put("model", CURRENT_MODEL);
        request.put("message", userMessage);
        request.put("temperature", 0.3);
        request.put("max_tokens", 800);

        URL url = new URL(COHERE_CHAT_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + COHERE_API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = request.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        InputStream stream =
                (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
            JSONObject json = new JSONObject(response.toString());
            if (json.has("text")) return json.getString("text").trim();
            if (json.has("message")) return json.getString("message").trim();
            return response.toString();
        }
    }

    // ---------- MESSAGE BUBBLES ----------

    private void addMessageBubble(String sender, String text, Color bg, int flowAlign) {
        JPanel wrapper = new JPanel(new FlowLayout(
                flowAlign == FlowLayout.LEFT ? FlowLayout.LEFT : FlowLayout.RIGHT,
                10, 6));
        wrapper.setOpaque(false);

        JLabel avatar = createAvatarLabel(sender.equals("You") ? "Y" : "C",
                sender.equals("You"));

        JTextArea message = new JTextArea(text);
        message.setFont(new Font("Inter", Font.PLAIN, 14));
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setOpaque(false);

        int maxWidth = 620;
        message.setSize(new Dimension(maxWidth, Short.MAX_VALUE));
        Dimension pref = message.getPreferredSize();
        message.setPreferredSize(new Dimension(Math.min(maxWidth, pref.width), pref.height));

        JPanel colorLayer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 14;
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, arc, arc);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 6, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        colorLayer.setOpaque(false);
        colorLayer.setBorder(new EmptyBorder(8, 10, 8, 10));
        colorLayer.add(message, BorderLayout.CENTER);

        JPanel bubble = new JPanel(new BorderLayout());
        bubble.setOpaque(false);
        bubble.add(colorLayer, BorderLayout.CENTER);

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

        if (flowAlign == FlowLayout.LEFT) {
            row.add(avatar);
            row.add(Box.createHorizontalStrut(10));
            row.add(bubble);
            row.add(Box.createHorizontalGlue());
        } else {
            row.add(Box.createHorizontalGlue());
            row.add(bubble);
            row.add(Box.createHorizontalStrut(10));
            row.add(avatar);
        }

        wrapper.add(row);
        messageList.add(wrapper);
        messageList.add(Box.createVerticalStrut(6));
        messageList.revalidate();
        messageList.repaint();
        scrollToBottom();
    }

    private JLabel createAvatarLabel(String text, boolean isUser) {
        JLabel avatar = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = isUser ? ACCENT : new Color(200, 200, 210);
                g2.setColor(fill);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Inter", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setOpaque(false);
        return avatar;
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    // ---------- Helper classes ----------

    static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final int radius;
        private final Color color;
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    static class PlaceholderTextField extends JTextField {
        private final String placeholder;
        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setColumns(1);
            setBorder(null);
            setMargin(new Insets(6, 6, 6, 6));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(getFont().deriveFont(Font.ITALIC,
                        getFont().getSize()));
                g2.setColor(new Color(165, 165, 175));
                FontMetrics fm = g2.getFontMetrics();
                int x = 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }

    static class RoundedButton extends JButton {
        private final Color bgColor;
        private final Color fgColor;

        public RoundedButton(String text, Color bg, Color fg) {
            super(text);
            this.bgColor = bg;
            this.fgColor = fg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(fgColor);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 18;
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(fgColor);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new CohereChatBot().setVisible(true);
        });
    }
}
