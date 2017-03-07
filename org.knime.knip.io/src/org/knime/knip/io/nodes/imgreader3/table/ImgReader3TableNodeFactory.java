package org.knime.knip.io.nodes.imgreader3.table;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.knip.cellviewer.CellNodeView;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ImgReader3TableNodeFactory<T extends NativeType<T> & RealType<T>>
		extends NodeFactory<ImgReader3TableNodeModel<T>> {

	@Override
	public ImgReader3TableNodeModel<T> createNodeModel() {
		return new ImgReader3TableNodeModel<>();
	}

	@Override
	protected int getNrNodeViews() {
		return 1;
	}

	@Override
	public NodeView<ImgReader3TableNodeModel<T>> createNodeView(final int viewIndex,
			final ImgReader3TableNodeModel<T> nodeModel) {
		return new CellNodeView<>(nodeModel);
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new ImgReader3TableNodeDialog();
	}

}
