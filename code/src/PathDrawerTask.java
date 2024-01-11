import java.awt.*;
import java.util.LinkedList;
import java.util.TimerTask;

// Lớp thực hiện việc vẽ đường đi trên lưới
class PathDrawerTask extends TimerTask {
    // Biến static để theo dõi vị trí hiện tại trong danh sách nút
    public static int currentIndex;
    // Danh sách các nút và lưới màu của chúng
    private LinkedList<Node> nodes;
    private Color[][] terrainGrid;

    // Constructor nhận danh sách nút và lưới màu và khởi tạo biến tương ứng
    public PathDrawerTask(LinkedList<Node> nodes, Color[][] terrainGrid) {
        this.nodes = nodes;
        this.terrainGrid = terrainGrid;
    }

    // Phương thức được gọi bởi Timer khi nhiệm vụ được thực hiện
    @Override
    public void run() {
        // Kiểm tra nếu danh sách nút ít hơn hoặc bằng 1 nút
        if (nodes.size() <= 1) {
            // Hiển thị thông báo và hủy bỏ TimerTask
            Platform.status.setText("No path found");
            this.cancel();
            return;
        }

        // Nếu là nút đầu tiên trong danh sách
        if (currentIndex == 0) {
            // Hiển thị thông báo về việc tìm thấy đường đi
            Platform.status.setText("Result: The path was found!");
        }

        // Nếu đã đi qua hết danh sách nút
        if (currentIndex >= nodes.size()) {
            // Hủy bỏ TimerTask để dừng thực thi
            this.cancel();
            return;
        }

        // Lấy nút hiện tại từ danh sách
        Node currentNode = nodes.get(currentIndex);
        // Lấy tọa độ X và Y của nút
        int x = (int) currentNode.getX();
        int y = (int) currentNode.getY();

        // Đặt màu của ô tương ứng trong lưới dựa trên vị trí của nút
        if (currentIndex == 0) {
            // Nếu là nút đầu tiên, đặt màu thành đỏ
            terrainGrid[x][y] = Color.RED;
        } else if (currentIndex == nodes.size() - 1) {
            // Nếu là nút cuối cùng, đặt màu thành xanh lá cây
            terrainGrid[x][y] = Color.GREEN;
        } else {
            // Nếu không phải nút đầu tiên hay cuối cùng, đặt màu thành màu vàng
            terrainGrid[x][y] = Color.YELLOW;
        }

        // Tăng vị trí hiện tại để chuyển sang nút tiếp theo trong danh sách
        currentIndex++;
    }
}
