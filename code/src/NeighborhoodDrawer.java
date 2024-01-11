import java.awt.*;
import java.util.LinkedList;
import java.util.TimerTask;

// Lớp NeighborhoodDrawer kế thừa từ TimerTask để thực hiện một công việc định kỳ
public class NeighborhoodDrawer extends TimerTask {
    // Biến static để theo dõi vị trí hiện tại trong danh sách nút
    public static int currentIndex;

    // Danh sách các nút và lưới màu của chúng
    private LinkedList<Node> nodes;
    private Color[][] terrainGrid;

    // Constructor nhận danh sách nút và lưới màu và khởi tạo biến tương ứng
    public NeighborhoodDrawer(LinkedList<Node> nodes, Color[][] terrainGrid) {
        this.nodes = nodes;
        this.terrainGrid = terrainGrid;
    }

    // Phương thức được gọi bởi Timer khi nhiệm vụ được thực hiện
    @Override
    public void run() {
        // Kiểm tra nếu đã đi qua hết danh sách nút
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

        // Đặt màu của ô tương ứng trong lưới thành CYAN
        terrainGrid[x][y] = Color.CYAN;

        // Tăng vị trí hiện tại để chuyển sang nút tiếp theo trong danh sách
        currentIndex++;
    }
}
