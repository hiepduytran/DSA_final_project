import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Timer;
import java.util.*;

public class Platform extends JPanel {

    // Quản lý số hàng và số cột trong lưới.
    public static int NUM_ROWS = 10;
    public static int NUM_COLS = 10;

    // Lưu trữ ID của nút bắt đầu và nút đích hiện tại.
    public static int currentStartId;
    public static int currentTargetId;

    // Xác định xem ID của nút bắt đầu và nút đích có được hiển thị không.
    public static boolean showStart = false;
    public static boolean showTarget = false;

    // Thông báo kết quả của thuật toán A* hoặc cảnh báo khi người dùng thiếu thông tin cần thiết
    public static JTextArea status;

    public static int percent = 25;
    public static Color[][] terrainGrid;

    public static int PREFERRED_GRID_SIZE_PIXELS = 600 / NUM_ROWS;

    // Mảng hai chiều chứa thông tin về chướng ngại vật trong lưới
    private static int[][] maps;

    // Sử dụng để lên lịch vẽ lưới và đường đi.
    public static Timer timer;

    // Giữ tham chiếu đến một đối tượng của lớp Platform.
    public static Platform ins;

    // Hàm khởi tạo của lớp, khởi tạo mảng maps và terrainGrid, thiết lập lưới và chướng ngại vật ban đầu.
    public Platform() {
        maps = new int[NUM_ROWS][NUM_COLS];
        terrainGrid = new Color[NUM_ROWS][NUM_COLS];
        setForm(NUM_ROWS, NUM_COLS, percent);
        if (ins == null) {
            ins = this;
        }
    }

    // Cài đặt lại kích thước lưới và tỉ lệ phần trăm ô màu đen (chướng ngại vật).
    public void setForm(int num_rows, int num_cols, int per) {
        NUM_ROWS = num_rows;
        NUM_COLS = num_cols;
        percent = per;
        PREFERRED_GRID_SIZE_PIXELS = 600 / num_rows;
        maps = new int[NUM_ROWS][NUM_COLS];
        terrainGrid = new Color[NUM_ROWS][NUM_COLS];
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                Random r = new Random();
                int randomInt = r.nextInt(100) + 1;
                if (randomInt <= percent) {
                    maps[i][j] = 0; // black (chướng ngại vật)
                } else {
                    maps[i][j] = 1; // white
                }
            }
        }
        createGraph();
        setTerrainGrid();
        int preferredWidth = NUM_COLS * PREFERRED_GRID_SIZE_PIXELS;
        int preferredHeight = NUM_ROWS * PREFERRED_GRID_SIZE_PIXELS;
        setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        repaint();
    }

    // Cài đặt màu sắc của lưới dựa trên thông tin về chướng ngại vật.
    public static void setTerrainGrid() {
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                if (maps[i][j] == 1) {
                    terrainGrid[i][j] = Color.WHITE;
                } else {
                    terrainGrid[i][j] = Color.BLACK;
                }
            }
        }
    }

    // Biểu diễn đồ thị và thực hiện thuật toán A*
    static Graph graph = new Graph();

    // Tạo lại đồ thị dựa trên mảng maps.
    public static void createGraph() {
        graph = new Graph();
        // Add nodes
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                if (maps[i][j] == 1) {
                    Node node = new Node(i, j);
                    node.setId(i * NUM_ROWS + j);
                    graph.addNode(node);
                }
            }

        }

        // Add edges
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                if (i > 0 && maps[i - 1][j] == 1) {
                    graph.addEdge(i * NUM_ROWS + j, (i - 1) * NUM_ROWS + (j), 1);
                }
                if (j > 0 && maps[i][j - 1] == 1) {
                    graph.addEdge(i * NUM_ROWS + j, (i) * NUM_ROWS + (j - 1), 1);
                }
                if (j < NUM_COLS - 1 && maps[i][j + 1] == 1) {
                    graph.addEdge(i * NUM_ROWS + j, (i) * NUM_ROWS + (j + 1), 1);
                }
                if (i < NUM_ROWS - 1 && maps[i + 1][j] == 1) {
                    graph.addEdge(i * NUM_ROWS + j, (i + 1) * NUM_ROWS + (j), 1);
                }
                if (i > 0 && j > 0 && maps[i - 1][j - 1] == 1) {
                    graph.addEdge(i * NUM_ROWS + j, (i - 1) * NUM_ROWS + (j - 1), 1);
                }
                if (i > 0 && j < NUM_COLS - 1 && maps[i - 1][j + 1] == 1) {
                    graph.addEdge(i * NUM_ROWS + j, (i - 1) * NUM_ROWS + (j + 1), 1);
                }
                if (i < NUM_ROWS - 1 && j > 0 && maps[i + 1][j - 1] == 1) {
                    graph.addEdge(i * NUM_ROWS + j, (i + 1) * NUM_ROWS + (j - 1), 1);
                }
                if (i < NUM_ROWS - 1 && j < NUM_COLS - 1 && maps[i + 1][j + 1] == 1) {
                    graph.addEdge(i * NUM_ROWS + j, (i + 1) * NUM_ROWS + (j + 1), 1);
                }
            }
        }
    }

    /*
    Gọi phương thức aStar() của đồ thị để tìm đường từ startId đến targetId.
    Lấy danh sách các nút trong đường đi và lưu vào biến nodes.
    Lên lịch vẽ các nút hàng xóm (neighbor) và đường đi (nodes) sử dụng Timer và TimerTask.
    */
    // Tìm đường đi từ nút bắt đầu đến nút đích và lên lịch vẽ đường đi.
    public int findPath(int startId, int targetId) {
        graph.aStar(startId, targetId);
        LinkedList<Node> nodes = graph.getPath(targetId);
        LinkedList<Node> neis = graph.nei;
        timer = new Timer();
        TimerTask task1 = new NeighborhoodDrawer(neis, terrainGrid);
        TimerTask task2 = new PathDrawerTask(nodes, terrainGrid);
        timer.scheduleAtFixedRate(task1, 0, 20);
        timer.scheduleAtFixedRate(task2, 20 * (neis.size() + 1), 20);

        if ((nodes.size() == 0 || nodes.size() == 1) && startId != targetId) {
            return -1;
        } else {
            timer.purge();
            return 0;
        }
    }

    // Lấy ID của node dựa trên tọa độ x, y trong JPanel.
    public static int getID(int x, int y) {
        int i = (x / PREFERRED_GRID_SIZE_PIXELS);
        int j = (y / PREFERRED_GRID_SIZE_PIXELS);
        return (j * NUM_ROWS) + i;
    }

    @Override
    // Phương thức vẽ thành phần của JPanel, bao gồm lưới và ID của nút.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Clear the board
        g.clearRect(0, 0, getWidth(), getHeight());

        // Draw the grid
        int rectWidth = getWidth() / NUM_COLS;
        int rectHeight = getHeight() / NUM_ROWS;

        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                int x = i * rectWidth;
                int y = j * rectHeight;
                Color terrainColor = terrainGrid[i][j];
                g.setColor(terrainColor);
                g.fillRect(y, x, rectWidth, rectHeight);
            }
        }
        if (showStart) {
            paintID(g, currentStartId, Color.RED);
        }

        if (showTarget) {
            paintID(g, currentTargetId, Color.GREEN);
        }

        repaint();
    }

    // Vẽ ID của nút trên lưới với màu được chỉ định.
    public static void paintID(Graphics g, int id, Color color) {
        g.setColor(color);
        if (color == Color.CYAN)
            System.out.println("neighbor");
        Node node = graph.getNode(id);
        if (node != null) {
            g.fillRect((int) node.getY() * PREFERRED_GRID_SIZE_PIXELS, (int) node.getX() * PREFERRED_GRID_SIZE_PIXELS,
                    PREFERRED_GRID_SIZE_PIXELS, PREFERRED_GRID_SIZE_PIXELS);
        }
    }

    // Xóa vẽ của ID của nút trên lưới
    public static void reMovePaintID(Graphics g, int id) {
        g.setColor(Color.WHITE);
        Node node = graph.getNode(id);
        if (node != null) {
            g.fillRect((int) node.getY() * PREFERRED_GRID_SIZE_PIXELS, (int) node.getX() * PREFERRED_GRID_SIZE_PIXELS,
                    PREFERRED_GRID_SIZE_PIXELS, PREFERRED_GRID_SIZE_PIXELS);
        }
    }

    // Lớp lắng nghe sự kiện chuột tùy chỉnh
    static class CustomMouseListener implements MouseListener {
        private JTextField textField;
        private JPanel map;
        String pos;

        public CustomMouseListener(JPanel map, JTextField textField, String pos) {
            this.textField = textField;
            this.map = map;
            this.pos = pos;
        }

        public void mouseClicked(MouseEvent e) {
            if (map.getMouseListeners().length != 0) {
                map.removeMouseListener(map.getMouseListeners()[0]);
            }
            map.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    textField.setText(String.valueOf(getID(e.getX(), e.getY())));
                    // paintID(map.getGraphics(), getID(e.getX(), e.getY()));
                    System.out.println(String.format("%d %d", e.getX(), e.getY()));
                    if (pos == "start") {
                        Platform.currentStartId = getID(e.getX(), e.getY());
                        Platform.showStart = true;
                    }
                    if (pos == "end") {
                        Platform.currentTargetId = getID(e.getX(), e.getY());
                        Platform.showTarget = true;
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("A star Algorithm");
                frame.setResizable(false);
                frame.setSize(5500, 5500);
                frame.setLayout(new GridLayout(1, 2));

                Platform map = new Platform();
                map.setForeground(Color.red);
                frame.add(map);

                JPanel panel = new JPanel();
                panel.setLayout(new GridBagLayout());
                panel.setSize(5500, 5500);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.ipady = 25;
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 4;
                JLabel create = new JLabel("CREATE MAPS", JLabel.CENTER);
                panel.add(create, gbc);

                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.ipady = 15;
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 1;
                JLabel row = new JLabel("Enter Rows: ", JLabel.RIGHT);
                panel.add(row, gbc);

                gbc.gridx = 1;
                gbc.gridy = 1;
                final JTextField num_rows = new JTextField(10);
                panel.add(num_rows, gbc);

                gbc.gridx = 2;
                gbc.gridy = 1;
                JLabel col = new JLabel("Enter Cols: ", JLabel.CENTER);
                panel.add(col, gbc);

                gbc.gridx = 3;
                gbc.gridy = 1;
                final JTextField num_cols = new JTextField(10);
                panel.add(num_cols, gbc);

                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.ipady = 15;
                gbc.gridx = 0;
                gbc.gridy = 2;
                JLabel per = new JLabel("Enter Per:", JLabel.LEFT);
                panel.add(per, gbc);

                gbc.gridx = 1;
                gbc.gridy = 2;
                final JTextField num_per = new JTextField(10);
                panel.add(num_per, gbc);

                gbc.gridx = 3;
                gbc.gridy = 2;
                gbc.gridwidth = 3;
                final JButton create_btn = new JButton("Create");
                panel.add(create_btn, gbc);

                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.ipady = 15;
                gbc.gridx = 0;
                gbc.gridy = 3;
                gbc.gridwidth = 4;
                JLabel test = new JLabel("A star Algorithm", JLabel.CENTER);
                panel.add(test, gbc);

                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.gridwidth = 1;
                gbc.gridx = 0;
                gbc.gridy = 4;
                JLabel start = new JLabel("Start: ", JLabel.RIGHT);

                panel.add(start, gbc);
                gbc.gridx = 1;
                gbc.gridy = 4;
                JTextField startID = new JTextField(10);
                panel.add(startID, gbc);

                JTextField targetID = new JTextField(10);

                CustomMouseListener m1 = new CustomMouseListener(map, startID, "start");
                CustomMouseListener m2 = new CustomMouseListener(map, targetID, "end");

                startID.addMouseListener(m1);
                targetID.addMouseListener(m2);

                // create target
                gbc.gridx = 2;
                gbc.gridy = 4;
                JLabel target = new JLabel("Target: ", JLabel.CENTER);
                panel.add(target, gbc);
                gbc.gridx = 3;
                gbc.gridy = 4;
                panel.add(targetID, gbc);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.gridx = 0;
                gbc.gridy = 5;
                gbc.gridwidth = 2;

                // create run
                JButton btstart = new JButton("Run");
                panel.add(btstart, gbc);
                gbc.gridx = 2;
                gbc.gridy = 5;

                JButton btReset = new JButton("Reset");
                panel.add(btReset, gbc);
                gbc.fill = GridBagConstraints.HORIZONTAL;

                status = new JTextArea("Result: ", 5, 3);
                status.setBackground(Color.gray);
                status.setForeground(Color.WHITE);
                gbc.gridwidth = 5;
                gbc.gridx = 0;
                gbc.gridy = 6;
                panel.add(status, gbc);

                btReset.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        btstart.setEnabled(true);
                        startID.setEnabled(true);
                        targetID.setEnabled(true);
                        Graph.ins.nei = new LinkedList<>();
                        NeighborhoodDrawer.currentIndex = 0;
                        if (timer != null)
                            timer.cancel();
                        setTerrainGrid();
                        map.repaint();
                        start.remove(startID);
                        target.remove(targetID);
                        startID.setText("");
                        targetID.setText("");
                        showStart = false;
                        showTarget = false;
                    }
                });
                btstart.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        btstart.setEnabled(false);
                        startID.setEnabled(false);
                        targetID.setEnabled(false);
                        setTerrainGrid();
                        boolean check1 = false;
                        boolean check2 = false;
                        int check = 0;
                        if (startID.getText().equals("")) {
                            check1 = true;
                        }
                        if (targetID.getText().equals("")) {
                            check2 = true;
                        }
                        String message = "";
                        if (check1) {
                            message += "Start ID is missing! Please choose ID!\n";
                            startID.setText("");
                            targetID.setText("");
                        }
                        if (check2) {
                            message += "Target ID is missing! Please choose ID!";
                            startID.setText("");
                            targetID.setText("");
                        }
                        status.setText(message);

                        if (!check1 && !check2) {
                            check = map.findPath(Integer.parseInt(startID.getText()),
                                    Integer.parseInt(targetID.getText()));
                            if (check == -1) {
                                // status.setText("Cannot find the path!");
                                startID.setText("");
                                targetID.setText("");
                            } else {
                                PathDrawerTask.currentIndex = 0;

                            }
                            map.repaint();
                        }
                    }

                });

                create_btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        map.setForm(Integer.parseInt(num_rows.getText()), Integer.parseInt(num_cols.getText()),
                                Integer.parseInt(num_per.getText()));
                        frame.repaint();
                        startID.setText("");
                        targetID.setText("");
                        if (timer != null) {
                            timer.cancel();
                        }
                        showStart = false;
                        showTarget = false;
                        startID.setEnabled(true);
                        targetID.setEnabled(true);
                        btstart.setEnabled(true);
                        Graph.ins.nei = new LinkedList<>();
                        NeighborhoodDrawer.currentIndex = 0;
                    }
                });

                // reset
                JButton btreset = new JButton("Reset");
                btreset.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setTerrainGrid();
                    }
                });

                frame.add(panel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}
