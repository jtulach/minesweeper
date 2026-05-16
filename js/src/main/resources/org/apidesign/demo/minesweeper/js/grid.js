/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2013-2020 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
function initializeGrid(gridSize, pieceCount) {
    class Grid {
        constructor(gridElement, gridContainer) {
            this.gridElement = gridElement;
            this.gridContainer = gridContainer;
            this.pieces = [];
            this.onDrop = [];
        }

        calculateCellSize() {
            const containerSize = this.gridContainer.clientWidth - 32; // 16px padding on each side
            return Math.floor(containerSize / gridSize);
        }

        calculatePieceSize() {
            return this.calculateCellSize() * 0.75;
        }

        getCellTargetPosition(row, col) {
            const cellSize = this.calculateCellSize();
            const pieceSize = this.calculatePieceSize();
            return {
                left: col * cellSize + (cellSize - pieceSize) / 2,
                top: row * cellSize + (cellSize - pieceSize) / 2
            };
        }

        backToTarget(col, row) {
            const piece = this.findPiece(row, col);
            if (piece !== null) {
                this.animatePieceBackToTarget(piece);
            }
        }

        animatePieceBackToTarget(piece) {
            const cellSize = this.calculateCellSize();
            const pieceSize = this.calculatePieceSize();
            const { centerX, centerY } = this.getTargetPosition(piece);
            const targetX = centerX - pieceSize / 2;
            const targetY = centerY - pieceSize / 2;

            piece.style.transition = 'left 0.3s ease, top 0.3s ease';
            piece.style.left = `${targetX}px`;
            piece.style.top = `${targetY}px`;

            piece.addEventListener('transitionend', event => {
                if (event.propertyName === 'left' || event.propertyName === 'top') {
                    if (!piece.classList.contains('at-target')) {
                        delete piece.dataset.gridRow;
                        delete piece.dataset.gridCol;
                        piece.classList.add('at-target');
                        this.updateGrid();
                    }
                }
            }, { once: true });
        }

        movePieceFromTargetToGridCell(col, row) {
            const availablePiece = this.pieces.find(piece => piece.classList.contains('at-target') && !piece.classList.contains('dragging'));
            if (!availablePiece) return false;

            const pieceSize = this.calculatePieceSize();
            const { left, top } = this.getCellTargetPosition(row, col, pieceSize);
            availablePiece.classList.remove('at-target');
            availablePiece.dataset.gridRow = row;
            availablePiece.dataset.gridCol = col;
            availablePiece.style.transition = '0.18s 0.5s ease';
            availablePiece.style.left = `${left}px`;
            availablePiece.style.top = `${top}px`;
            availablePiece.addEventListener('transitionend', event => {
                let cancelled = availablePiece.classList.contains('at-target');
                if (event.propertyName === 'left' || event.propertyName === 'top') {
                    if (!cancelled) {
                        this.completePieceDrop(availablePiece);
                    }
                    this.updateGrid();
                }
            }, { once: true });
            availablePiece.addEventListener('transitioncancel', event => {
                if (!availablePiece.classList.contains('at-target')) {
                    delete availablePiece.dataset.gridCol;
                    delete availablePiece.dataset.gridRow;
                    availablePiece.classList.add('at-target');
                }
            }, { once: true });
            return true;
        }

        completePieceDrop(piece) {
            const cellSize = this.calculateCellSize();
            const pieceSize = this.calculatePieceSize();
            const left = parseFloat(piece.style.left);
            const top = parseFloat(piece.style.top);
            const snapped = this.getSnappedGridPosition(left, top);

            piece.classList.remove('dragging');

            if (snapped) {
                const cell = this.getGridCoordinates(snapped.left, snapped.top);
                if (cell && this.findPiece(cell.row, cell.col, piece) !== null) {
                    this.animatePieceBackToTarget(piece);
                    return;
                }
                let prevCol = piece.dataset.gridCol || -1;
                let prevRow = piece.dataset.gridRow || -1;
                if (!this.onDrop.some(f => f(prevCol, prevRow, cell.col, cell.row))) {
                    this.animatePieceBackToTarget(piece);
                    return;
                }

                piece.dataset.gridRow = cell.row;
                piece.dataset.gridCol = cell.col;
                piece.classList.remove('at-target');
                piece.style.transition = 'left 0.18s ease, top 0.18s ease';
                piece.style.left = `${snapped.left}px`;
                piece.style.top = `${snapped.top}px`;
            } else {
                this.animatePieceBackToTarget(piece);
            }
        }

        updateGrid() {
            const cellSize = this.calculateCellSize();
            const pieceSize = this.calculatePieceSize();
            document.documentElement.style.setProperty('--cell-size', `${cellSize}px`);
            this.gridElement.style.setProperty('--grid-size', gridSize);

            const pieceOffset = (cellSize - pieceSize) / 2;

            this.pieces.forEach(piece => {
                if (piece.classList.contains('dragging')) return;
                piece.style.transition = 'none';

                if (piece.classList.contains('at-target')) {
                    const { centerX, centerY } = this.getTargetPosition(piece);
                    piece.style.left = `${centerX - pieceSize / 2}px`;
                    piece.style.top = `${centerY - pieceSize / 2}px`;
                } else if (piece.dataset.gridRow !== undefined && piece.dataset.gridCol !== undefined) {
                    const row = parseInt(piece.dataset.gridRow, 10);
                    const col = parseInt(piece.dataset.gridCol, 10);
                    piece.style.left = `${col * cellSize + pieceOffset}px`;
                    piece.style.top = `${row * cellSize + pieceOffset}px`;
                }
            });

        }

        createPieces(dragController) {
            const cellSize = this.calculateCellSize();
            const pieceSize = this.calculatePieceSize();
            const positions = new Set();

            while (positions.size < pieceCount) {
                const randomIndex = Math.floor(Math.random() * gridSize * gridSize);
                positions.add(randomIndex);
            }

            const pieceOffset = (cellSize - pieceSize) / 2;

            this.pieces = Array.from(positions).map(index => {
                const row = Math.floor(index / gridSize);
                const col = index % gridSize;
                const piece = document.createElement('div');
                piece.className = 'piece';
                piece.dataset.gridRow = row;
                piece.dataset.gridCol = col;
                piece.style.left = `${col * cellSize + pieceOffset}px`;
                piece.style.top = `${row * cellSize + pieceOffset}px`;
                dragController.attach(piece);
                this.gridElement.appendChild(piece);
                return piece;
            });

            return this.pieces;
        }

        getRemainingPieces() {
            return this.pieces.reduceRight((sum, p) => {
                let isRemaining = p.classList.contains('at-target');
                return isRemaining ? sum + 1 : sum;
            }, 0);
        }

        getTargetPosition(piece) {
            let cellSize = this.calculateCellSize();
            let cellHalf = cellSize / 2;
            let index = this.findIndex(piece);

            let centerX, centerY;

            if (window.innerWidth > window.innerHeight) {
                centerX = gridSize * cellSize + cellHalf;
                centerY = cellHalf + index * cellSize;
            } else {
                centerX = cellHalf + index * cellSize;
                centerY = -cellHalf;
            }
            return { centerX, centerY };
        }

        getSnappedGridPosition(left, top) {
            const pieceSize = this.calculatePieceSize();
            const gridWidth = this.gridElement.clientWidth;
            const gridHeight = this.gridElement.clientHeight;

            if (left < 0 || top < 0 || left + pieceSize > gridWidth || top + pieceSize > gridHeight) {
                return null;
            }

            const centerX = left + pieceSize / 2;
            const centerY = top + pieceSize / 2;
            const cellWidth = gridWidth / gridSize;
            const cellHeight = gridHeight / gridSize;
            const col = Math.min(gridSize - 1, Math.max(0, Math.floor(centerX / cellWidth)));
            const row = Math.min(gridSize - 1, Math.max(0, Math.floor(centerY / cellHeight)));
            const snappedLeft = col * cellWidth + (cellWidth - pieceSize) / 2;
            const snappedTop = row * cellHeight + (cellHeight - pieceSize) / 2;

            return { left: snappedLeft, top: snappedTop };
        }

        getGridCoordinates(left, top) {
            const pieceSize = this.calculatePieceSize();
            const gridWidth = this.gridElement.clientWidth;
            const gridHeight = this.gridElement.clientHeight;
            if (left < 0 || top < 0 || left + pieceSize > gridWidth || top + pieceSize > gridHeight) {
                return null;
            }

            const cellWidth = gridWidth / gridSize;
            const cellHeight = gridHeight / gridSize;
            const centerX = left + pieceSize / 2;
            const centerY = top + pieceSize / 2;
            const col = Math.min(gridSize - 1, Math.max(0, Math.floor(centerX / cellWidth)));
            const row = Math.min(gridSize - 1, Math.max(0, Math.floor(centerY / cellHeight)));

            return { row, col };
        }

        findPiece(row, col, excludedPiece) {
            let at = this.pieces.findIndex(other => {
                if (other === excludedPiece) return false;
                if (other.classList.contains('at-target')) return false;

                const otherLeft = parseFloat(other.style.left);
                const otherTop = parseFloat(other.style.top);
                const otherPieceSize = parseFloat(other.dataset.pieceSize);
                const coords = this.getGridCoordinates(otherLeft, otherTop);
                return coords && coords.row === row && coords.col === col;
            });
            return at >= 0 ? this.pieces[at] : null;
        }

        findIndex(piece) {
            return this.pieces.indexOf(piece);
        }
    }

    class DragController {
        constructor(grid) {
            this.grid = grid;
            this.piece = null;
            this.offsetX = 0;
            this.offsetY = 0;
            this.startedAtTarget = false;
        }

        attach(piece) {
            piece.addEventListener('pointerdown', this.onPointerDown.bind(this));
            piece.addEventListener('pointermove', this.onPointerMove.bind(this));
            piece.addEventListener('pointerup', this.onPointerUp.bind(this));
            piece.addEventListener('pointercancel', this.onPointerCancel.bind(this));
        }

        onPointerDown(event) {
            event.preventDefault();
            const piece = event.currentTarget;
            const rect = piece.getBoundingClientRect();
            const pointerX = event.clientX - rect.left;
            const pointerY = event.clientY - rect.top;

            this.piece = piece;
            this.offsetX = pointerX;
            this.offsetY = pointerY;
            this.startedAtTarget = piece.classList.contains('at-target');

            if (this.startedAtTarget) {
                piece.classList.remove('at-target');
            }

            piece.setPointerCapture(event.pointerId);
            piece.style.transition = 'none';
            piece.classList.add('dragging');
        }

        onPointerMove(event) {
            if (!this.piece) return;

            const piece = this.piece;
            const gridRect = this.grid.gridElement.getBoundingClientRect();
            const left = event.clientX - gridRect.left - this.offsetX;
            const top = event.clientY - gridRect.top - this.offsetY;

            piece.style.left = `${left}px`;
            piece.style.top = `${top}px`;
        }

        onPointerUp(event) {
            if (!this.piece) return;

            const piece = this.piece;
            piece.releasePointerCapture(event.pointerId);
            this.grid.completePieceDrop(piece);
            this.piece = null;
        }

        onPointerCancel() {
            if (!this.piece) return;

            this.grid.completePieceDrop(this.piece);
            this.piece = null;
        }
    }

    const gridElement = document.getElementById('grid');
    const gridContainer = document.querySelector('.grid-container');
    const gridManager = new Grid(gridElement, gridContainer);
    const PIECE_SPEED = 420; // pixels per second
    const dragController = new DragController(gridManager);

    function animatePieces(pieces) {
        const cellSize = gridManager.calculateCellSize();
        const pieceSize = gridManager.calculatePieceSize();

        pieces.forEach(piece => {
            const { centerX, centerY } = gridManager.getTargetPosition(piece);
            const targetX = centerX - pieceSize / 2;
            const targetY = centerY - pieceSize / 2;
            const startX = parseFloat(piece.style.left);
            const startY = parseFloat(piece.style.top);
            const dx = targetX - startX;
            const dy = targetY - startY;
            const distance = Math.hypot(dx, dy);
            const duration = Math.max(0.2, distance / PIECE_SPEED);

            piece.style.transition = `transform ${duration}s ease-in-out`;
            piece.addEventListener('transitionend', event => {
                if (event.propertyName === 'transform') {
                    const targetLeft = targetX;
                    const targetTop = targetY;
                    piece.style.transition = 'none';
                    piece.style.left = `${targetLeft}px`;
                    piece.style.top = `${targetTop}px`;
                    piece.style.transform = 'none';
                    piece.classList.add('at-target');
                    delete piece.dataset.gridRow;
                    delete piece.dataset.gridCol;
                }
            }, { once: true });

            requestAnimationFrame(() => {
                piece.style.transform = `translate(${dx}px, ${dy}px)`;
                piece.classList.add('moving');
            });
        });
    }

    // Handle window resize for responsiveness
    window.addEventListener('resize', () => {
        gridManager.updateGrid();
    });

    var pieces = null;
    return {
        'updateGrid' : function() {
            gridManager.updateGrid();
            if (pieces === null) {
                pieces = gridManager.createPieces(dragController);
            }
            animatePieces(pieces);
        },
        'registerDrop' : function(f) {
            gridManager.onDrop.push(f);
        },
        'moveTo' : function(x, y) {
            gridManager.movePieceFromTargetToGridCell(x, y);
        },
        'backToTarget' : function(x, y) {
            gridManager.backToTarget(x, y);
        }
    };
}
